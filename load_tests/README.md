## 1) Instalar dependencias

No diretório de testes de carga (com seu ambiente virtual Python ativado):

```bash
pip install locust
```

## 2) Preparar credenciais

Copie o arquivo exemplo e preencha com usuários reais cadastrados no banco de dados local:

```bash
cp users.example.csv users.csv
```

Formato CSV (`users.csv`):

- `email`: e-mail do usuário
- `senha`: senha do usuário
- `role`: `nutricionista` ou `admin`
- `lembrar_de_mim`: `true` ou `false`

## 3) Subir o Backend (Victus / SistemaNutricao)

No terminal ou na sua IDE (IntelliJ), inicie a aplicação Spring Boot (geralmente em `http://localhost:8080`).

Se for rodar via terminal PowerShell:
```powershell
cd c:\Users\Win10\IdeaProjects\Victus
.\mvnw.cmd spring-boot:run
```

## 4) Executar o Locust

No diretório `load_tests` (com o venv ativo):

```bash
locust -f locustfile.py --host=http://localhost:8080
```

Abra a interface web do Locust em [http://localhost:8089](http://localhost:8089).

## 5) Perfis e Navegação Simulada

- `PublicVisitorUser`:
  - `GET /login`
- `NutricionistaUser` / `AdminUser`:
  - Login via Form POST (`POST /login`)
  - `GET /home`
  - `GET /ingrediente`
  - `GET /ingrediente/taco`
  - `GET /ingrediente/api/buscar?q=...`
  - `GET /refeicao`
  - `GET /ficha`
  - `GET /admin/usuarios` (somente admin)
  - `GET /admin/estabelecimentos` (somente admin)
  - Logout (`POST /sair-do-sistema`)

## 6) Execução Sem Interface (Headless)

```bash
locust -f locustfile.py --host=http://localhost:8080 --headless --users 50 --spawn-rate 5 --run-time 2m
```