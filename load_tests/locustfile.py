import csv
import os
import random
import threading
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Optional

from locust import HttpUser, between, events, task


def _env_int(name: str, default: int) -> int:
    value = os.getenv(name)
    if value is None:
        return default
    try:
        return int(value)
    except ValueError:
        return default


def _env_float(name: str, default: float) -> float:
    value = os.getenv(name)
    if value is None:
        return default
    try:
        return float(value)
    except ValueError:
        return default


def _env_bool(name: str, default: bool = False) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "t", "yes", "y", "on"}


def _normalize_role(role: str) -> str:
    normalized = (role or "").strip().lower()
    if normalized in {"admin", "administrador"}:
        return "admin"
    if normalized in {"nutricionista", "nutri"}:
        return "nutricionista"
    if normalized in {"producao", "produção"}:
        return "producao"
    return "nutricionista"


@dataclass
class Credential:
    email: str
    senha: str
    role: str
    lembrar_de_mim: bool = False


class CredentialStore:
    def __init__(self, credentials: List[Credential]):
        self._credentials_by_role: Dict[str, List[Credential]] = defaultdict(list)
        for credential in credentials:
            self._credentials_by_role[credential.role].append(credential)
            self._credentials_by_role["all"].append(credential)
        self._lock = threading.Lock()
        self._cursors: Dict[str, int] = defaultdict(int)

    def next_for_role(self, role: str) -> Optional[Credential]:
        role_key = _normalize_role(role)
        with self._lock:
            bucket = self._credentials_by_role.get(role_key) or self._credentials_by_role.get("all")
            if not bucket:
                return None
            cursor = self._cursors[role_key] % len(bucket)
            self._cursors[role_key] += 1
            return bucket[cursor]


def _resolve_credentials_file() -> Path:
    explicit = os.getenv("LOCUST_USERS_CSV")
    if explicit:
        return Path(explicit).expanduser().resolve()
    return (Path(__file__).parent / "users.csv").resolve()


def _load_credentials() -> CredentialStore:
    credentials: List[Credential] = []

    single_email = os.getenv("LOCUST_SINGLE_USER_EMAIL")
    single_password = os.getenv("LOCUST_SINGLE_USER_PASSWORD")
    single_role = _normalize_role(os.getenv("LOCUST_SINGLE_USER_ROLE", "nutricionista"))
    if single_email and single_password:
        credentials.append(
            Credential(
                email=single_email,
                senha=single_password,
                role=single_role,
                lembrar_de_mim=_env_bool("LOCUST_SINGLE_USER_REMEMBER", False),
            )
        )

    users_csv = _resolve_credentials_file()
    if users_csv.exists():
        with users_csv.open("r", encoding="utf-8") as file:
            reader = csv.DictReader(file)
            for row in reader:
                email = (row.get("email") or "").strip()
                senha = (row.get("senha") or "").strip()
                if not email or not senha:
                    continue
                role = _normalize_role(row.get("role", "nutricionista"))
                credentials.append(
                    Credential(
                        email=email,
                        senha=senha,
                        role=role,
                        lembrar_de_mim=str(row.get("lembrar_de_mim", "false")).strip().lower() in {"1", "true", "yes"},
                    )
                )

    return CredentialStore(credentials)


CREDENTIALS = _load_credentials()


class SistemaNutricaoBaseUser(HttpUser):
    abstract = True
    wait_time = between(
        _env_float("LOCUST_WAIT_MIN", 0.4),
        _env_float("LOCUST_WAIT_MAX", 2.0),
    )


class PublicVisitorUser(SistemaNutricaoBaseUser):
    weight = _env_int("LOCUST_WEIGHT_VISITOR", 2)

    @task(1)
    def visit_login(self):
        self.client.get("/login", name="GET /login")


class AuthenticatedNavigationUser(SistemaNutricaoBaseUser):
    abstract = True
    required_role = "nutricionista"
    weight = 1

    def on_start(self):
        credential = CREDENTIALS.next_for_role(self.required_role)
        if not credential:
            self.environment.runner.quit()
            raise RuntimeError(
                "Nenhuma credencial encontrada. Informe LOCUST_USERS_CSV com email,senha,role ou use LOCUST_SINGLE_USER_EMAIL/LOCUST_SINGLE_USER_PASSWORD."
            )

        # SistemaNutricao uses form-encoded login, not JSON
        login_response = self.client.post(
            "/login",
            data={
                "email": credential.email,
                "password": credential.senha,
                "remember-me": "on" if credential.lembrar_de_mim else "",
            },
            name="POST /login",
            allow_redirects=False # Prevent following redirect to check status
        )

        if login_response.status_code not in (200, 302):
            raise RuntimeError(f"Falha no login para {credential.email}: HTTP {login_response.status_code}")

    def on_stop(self):
        self.client.post("/sair-do-sistema", name="POST /sair-do-sistema")

    @task(2)
    def check_home(self):
        self.client.get("/home", name="GET /home")

    @task(3)
    def list_ingredientes(self):
        self.client.get("/ingrediente", name="GET /ingrediente")

    @task(2)
    def list_taco(self):
        self.client.get("/ingrediente/taco", name="GET /ingrediente/taco")

    @task(1)
    def api_buscar(self):
        query = random.choice(["arroz", "feijao", "frango", "batata", "leite", "ovo"])
        self.client.get(f"/ingrediente/api/buscar?q={query}", name="GET /ingrediente/api/buscar")

    @task(2)
    def list_refeicoes(self):
        self.client.get("/refeicao", name="GET /refeicao")

    @task(2)
    def list_fichas(self):
        self.client.get("/ficha", name="GET /ficha")


class NutricionistaUser(AuthenticatedNavigationUser):
    required_role = "nutricionista"
    weight = _env_int("LOCUST_WEIGHT_NUTRI", 4)


class AdminUser(AuthenticatedNavigationUser):
    required_role = "admin"
    weight = _env_int("LOCUST_WEIGHT_ADMIN", 1)

    @task(2)
    def check_admin_usuarios(self):
        self.client.get("/admin/usuarios", name="GET /admin/usuarios")

    @task(2)
    def check_admin_estabelecimentos(self):
        self.client.get("/admin/estabelecimentos", name="GET /admin/estabelecimentos")


@events.test_start.add_listener
def on_test_start(environment, **kwargs):
    print("[locust] Teste iniciado para SistemaNutricao")
    print(f"[locust] Host alvo: {environment.host}")
    print(f"[locust] CSV de usuarios: {_resolve_credentials_file()}")
