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

## 7) Gerando Massa de Dados (Teste de Carga)

Se você precisar de muitos dados para testar o sistema sob carga sem ter que cadastrar manualmente, existe um script SQL pronto para gerar um cenário robusto. Ele criará de uma vez:
- **X Ingredientes**
- **X Refeições**
- **X Fichas Técnicas** (incluindo Preparações e Perfis Nutricionais)
- Vínculos entre as Refeições e as Fichas Técnicas
- Vínculos entre os Ingredientes e as Fichas Técnicas

*(Onde **X** é a quantidade que você quiser definir)*

O arquivo está em `load_tests/insert.sql`.

### Como rodar em ambiente Docker (Produção / VPS):

1. Envie o script para dentro do container do MySQL:
```bash
cat load_tests/insert.sql | docker exec -i nutricao-mysql mysql -u root -p'SUA_SENHA' seu_banco
```

2. Entre no console do MySQL no container:
```bash
docker exec -it nutricao-mysql mysql -u root -p'SUA_SENHA' seu_banco
```

3. Dentro do MySQL, liste os usuários para pegar o ID do nutricionista que receberá os dados:
```sql
SELECT id, email, cargo FROM usuario;
```

4. Execute a Procedure para gerar a massa de dados, passando o **ID do usuário** (ex: `1`) e a **quantidade desejada** (ex: `100`, `1000`, `5000`):
```sql
-- Exemplo gerando 100 itens (fichas, ingredientes e refeições) para o usuário de ID 1:
CALL InsertFichasLote(1, 100);
```

### Como limpar a massa de teste depois:

Para não deixar o banco sujo, entre no console MySQL e rode os comandos abaixo para apagar tudo que foi criado pelo teste de forma segura:

```sql
SET FOREIGN_KEY_CHECKS=0;

DELETE FROM fichas_por_refeicao;
DELETE FROM ingredientes_por_ficha;
DELETE FROM refeicao WHERE nome LIKE 'Refeição Teste Carga %';
DELETE FROM ingrediente WHERE nome LIKE 'Ingrediente Teste Carga %';
DELETE FROM preparacao WHERE nome LIKE 'Ficha de Teste de Carga %';
DELETE FROM ficha_tecnica WHERE custo_per_capita = 15.50 AND status = 'ATIVA';
DELETE FROM perfil_nutricional WHERE vtc = 500.00 AND kcalptn = 100.00;

SET FOREIGN_KEY_CHECKS=1;
```