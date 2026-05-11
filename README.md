# Victus - Gestão Nutricional

O **Victus - Gestão Nutricional** é um sistema robusto desenvolvido para auxiliar profissionais de nutrição e equipes de produção na gestão técnica e operacional de serviços de alimentação. O sistema permite o controle detalhado de ingredientes, a criação de fichas técnicas complexas com cálculos automáticos de perfil nutricional e custos, além da organização de refeições completas.

Com uma interface moderna e funcional, o Victus utiliza tecnologias como Spring Boot e HTMX para proporcionar uma experiência fluida, permitindo buscas dinâmicas, exportação de relatórios em PDF e uma gestão eficiente de estabelecimentos e usuários.

## Controllers

Abaixo estão detalhados os controladores que compõem a lógica de negócio do sistema:

### 1. AdminController

Gerencia a parte administrativa do sistema, sendo restrito a usuários com perfil de **ADMIN**.

- **Usuários:** Permite a listagem paginada de usuários, ativação/desativação de contas e alteração de cargos.
- **Estabelecimentos:** Gerencia os locais vinculados ao sistema (criação, edição e listagem), permitindo vincular usuários a estabelecimentos específicos.

### 2. FichaController

O coração do sistema, responsável pela gestão das **Fichas Técnicas de Preparo**.

- **Operações:** Permite a criação, edição, visualização detalhada e exclusão (logica) de fichas.
- **Pesquisa Avançada:** Possui filtros dinâmicos por nome, categoria, número, VTC e macronutrientes (PTN, CHO, LIP, Sódio, Gordura Saturada).
- **Exportação:** Gera PDF para cada ficha técnica.
- **Acesso:** Disponível para os cargos de **NUTRICIONISTA** e **PRODUÇÃO**.

### 3. IngredienteController

Gerencia o banco de dados de ingredientes fundamentais para a composição das fichas técnicas.

- **Integração TACO:** Permite a consulta e utilização de dados da Tabela Brasileira de Composição de Alimentos (TACO).
- **Meus Ingredientes:** Possibilita que o nutricionista cadastre ingredientes personalizados.
- **Funcionalidades:** Listagem paginada, busca por nutrientes e gerenciamento de status (ativo/inativo).

### 4. RefeicaoController

Responsável pela composição de refeições que agrupam diversas fichas técnicas.

- **Cálculo Nutricional:** Calcula automaticamente os nutrientes totais da refeição somando as fichas técnicas vinculadas.
- **Gestão:** Permite organizar o cardápio e gerenciar a disponibilidade das refeições no sistema.

### 5. UsuarioController

Lida com a segurança, autenticação e identidade dos usuários.

- **Autenticação:** Gerencia o fluxo de login, logout e verificação de status de sessão.
- **Perfil:** Permite que o usuário visualize e edite suas informações pessoais, incluindo o upload de fotos de perfil.
- **Registro:** Controla o fluxo de cadastro de novos usuários no sistema.

### 6. UsernameController (Controller Advice)

Atua como um suporte global para as views do sistema.

- **Contexto:** Injeta automaticamente as informações do usuário logado (nome e objeto de perfil) em todos os modelos de visualização, garantindo que o cabeçalho e outras partes da interface exibam os dados corretos do usuário atual.

### 7. GlobalExceptionHandler (Controller Advice)

Centraliza o tratamento de erros do sistema de forma global.

- **Consistência:** Captura exceções da seção Exceptions e garante que o usuário receba mensagens de erro amigáveis e estruturadas, mantendo a estabilidade visual da aplicação mesmo em casos de falha inesperada.

### 8. PaginacaoViewSupport (Support)

Componente auxiliar utilizado por diversos controladores para padronizar a navegação e a renderização de telas.

- **Paginação:** Calcula e injeta no modelo os atributos necessários para a navegação paginada, como a URL da próxima página e indicadores de continuidade de resultados.
- **Renderização Dinâmica:** Gerencia a integração com o **HTMX**, decidindo se deve retornar a página completa com o Layout, apenas o conteúdo principal ou fragmentos específicos de listas, otimizando o tráfego de dados e a performance do frontend.

## Exceptions

O sistema utiliza uma hierarquia de exceções personalizadas para garantir que erros de negócio e falhas de integridade sejam tratados de forma consistente. Abaixo estão todas as classes de exceção implementadas:

### 1. DomainException

A classe base abstrata para todas as exceções de domínio do sistema. Estende `RuntimeException` e serve como raiz para garantir que erros específicos da regra de negócio sejam capturados separadamente de erros genéricos de infraestrutura.

### 2. ValidationException

Exceção base para erros de validação de regras de negócio. É utilizada para sinalizar que os dados fornecidos não atendem aos requisitos lógicos do sistema antes de qualquer operação de persistência.

### 3. FormValidationException

Especialização de `ValidationException` voltada para o contexto de entrada de dados via formulários. É anotada com `@ResponseStatus(HttpStatus.BAD_REQUEST)`, garantindo que o cliente receba um status 400 quando houver falhas de preenchimento.

### 4. DuplicateNomeException

Lançada quando ocorre uma tentativa de cadastrar uma entidade (como Ingrediente ou Ficha Técnica) com um nome que já existe no sistema para aquele contexto, preservando a unicidade dos dados.

### 5. EntityNotFoundException

Classe base para erros de busca onde um recurso esperado não foi localizado. Serve como pai para todas as exceções de "não encontrado", permitindo um tratamento genérico ou específico conforme a necessidade.

### 6. UsuarioNotFoundException

Especialização de `EntityNotFoundException` disparada especificamente quando um usuário não é encontrado pelo seu ID ou outro identificador único durante operações de autenticação ou gestão.

### 7. IngredienteNotFoundException

Especialização utilizada quando um ingrediente solicitado (seja do banco próprio ou da tabela TACO) não existe na base de dados.

### 8. FichaTecnicaNotFoundException

Garante o tratamento de erro quando uma Ficha Técnica de Preparo específica não é localizada, impedindo a exibição de detalhes ou edição de registros inexistentes.

### 9. RefeicaoNotFoundException

Acionada em buscas de refeições por ID quando o registro não consta no banco de dados, sendo essencial para a integridade do módulo de cardápios.

### 10. EstabelecimentoNotFoundException

Utilizada no módulo administrativo para sinalizar que um estabelecimento vinculado a usuários ou operações não foi encontrado.

### 11. UsuarioSemCargoException

Exceção de segurança crítica, lançada quando um usuário autenticado tenta realizar operações que exigem um cargo (Role/Cargo) mas o seu perfil ainda não possui um atribuído.

## Mappers

O sistema utiliza a biblioteca **MapStruct** para gerenciar a transformação de dados entre as Entidades JPA e os DTOs (Records). Garante alta performance (através de geração de código em tempo de compilação sem reflexão), tipagem forte e redução de código repetitivo.

### 1. Arquitetura MapStruct

- **Interfaces:** Todos os mappers são definidos como interfaces Java anotadas com `@Mapper(componentModel = "spring")`, permitindo que o Spring os gerencie como beans e os injete nos serviços.
- **Segurança:** Conflitos de nomenclatura ou tipos incompatíveis são detectados durante a compilação do Maven, impedindo erros em tempo de execução.
- **Customização:** Mapeamentos complexos (como aninhamento de objetos ou campos com nomes divergentes) são resolvidos via anotações `@Mapping`.

### 2. Mappers Implementados

- **FichaTecnicaMapper:** Gerencia as transformações da entidade `FichaTecnica`, incluindo o mapeamento profundo de `Preparacao`, `PerfilNutricional` e a lista de `IngredientesPorFicha`.
- **FichaFormMapper:** Especializado em converter DTOs de visualização em DTOs de atualização, facilitando o preenchimento de formulários complexos.
- **IngredienteMapper:** Converte a entidade `Ingrediente` para `IngredienteGetDTO` e DTOs especializados com Tags nutricionais.
- **RefeicaoMapper:** Transforma a entidade `Refeicao` em `RefeicaoResponseDTO`, gerenciando a coleção de fichas vinculadas.
- **UsuarioMapper:** Converte dados de `Usuario`, abstraindo relacionamentos com `Estabelecimento` e garantindo que apenas dados necessários cheguem à view.

## Model

O coração dos dados do Victus é composto por entidades JPA que representam o domínio da nutrição e gestão. A arquitetura utiliza relacionamentos complexos para garantir a integridade dos cálculos nutricionais e de custos.

### Entidades Principais

#### 1. Usuario

Entidade central de segurança e propriedade.

- **Atributos:** Username, email, senha (criptografada), cargo, status de ativação e caminho da foto de perfil.
- **Relacionamentos:** Pertence a um `Estabelecimento` e é dono de seus `Ingredientes`, `FichasTecnicas` e `Refeicoes`.

#### 2. FichaTecnica

Representa o documento técnico de uma preparação.

- **Dados:** Custos (total e per capita), número de porções, peso da porção e medida caseira.
- **Composição:** Possui uma `Preparacao` (detalhes executivos) e um `PerfilNutricional` (resumo macro).
- **Relacionamentos:** Contém uma lista de `IngredientesPorFicha`.

#### 3. Ingrediente

O componente básico de construção.

- **Nutrientes:** Proteína, Carboidrato, Lipídios, Sódio e Gordura Saturada por 100g.
- **Propriedade:** Cada ingrediente pertence a um usuário ou ao sistema (TACO).

#### 4. Refeicao

Agrupador de fichas técnicas para planejamento de cardápio.

- **Cálculos:** Soma automaticamente as calorias e macronutrientes de todas as fichas vinculadas.
- **Relacionamentos:** Conectada a múltiplas fichas através da entidade de ligação `FichasPorRefeicao`.

#### 5. Estabelecimento

Unidade organizacional do sistema.

- **Função:** Permite agrupar usuários e isolar dados administrativos por unidade de negócio.

### Entidades de Ligação e Auxiliares

- **IngredientesPorFicha:** A ponte entre Ficha e Ingrediente. Armazena o **Peso Bruto (PB)**, **Peso Líquido (PL)** e o **Fator de Correção (FC)**, calculando o custo e os nutrientes específicos para aquela quantidade.
- **Preparacao:** Armazena o "como fazer" (tempo, equipamentos, modo de preparo, rendimento e FCC).
- **PerfilNutricional:** Consolida os valores totais de calorias e macronutrientes de uma ficha ou refeição.
- **FichasPorRefeicao:** Entidade de ligação Many-to-Many entre Refeição e Ficha Técnica.

### Enums (Domínio)

- **Cargo:** Define as permissões (`ADMIN`, `NUTRICIONISTA`, `PRODUCAO`).
- **Categoria:** Classifica as fichas (`ACOMPANHAMENTO`, `PRATO_PRINCIPAL`, `ENTRADA`, etc.).
- **Status:** Controla a visibilidade (`ATIVA`, `INATIVA`).
- **StatusCriacao:** Gerencia o fluxo de trabalho (`RASCUNHO`, `COMPLETA`).
- **Tag:** Identificadores nutricionais visuais (`ALTO_SODIO`, `BAIXA_GORDURA`, etc.).

## Records (DTOs)

O sistema utiliza **Java Records** para implementar o padrão DTO (_Data Transfer Object_). Os Records são ideais para essa função por serem imutáveis, concisos e focados puramente em transportar dados entre a camada de persistência e a interface do usuário.

### Organização e Propósitos

Os DTOs estão organizados por domínio e finalidade, garantindo que apenas os dados necessários sejam trafegados em cada operação:

#### 1. Ficha Técnica

- **FichaTecnicaCreateDTO:** Captura os dados necessários para a criação de uma nova ficha, incluindo listas de ingredientes e dados de preparação.
- **FichaTecnicaUpdateDTO:** Utilizado para atualizações, permitindo modificar campos específicos de uma ficha existente.
- **FichaTecnicaGetDTO:** O formato padrão de saída, contendo todos os dados calculados e formatados para exibição.
- **FichaTecnicaComTagDTO:** Especializado para listagens que exibem alertas nutricionais (tags).

#### 2. Ingredientes

- **IngredienteDTO:** Utilizado para entrada de dados básicos de novos ingredientes.
- **IngredienteGetDTO:** Retorna os dados do ingrediente, incluindo os valores nutricionais por 100g.
- **IngredienteComTagDTO:** Versão estendida que inclui a classificação de nutrientes (Tags).
- **IngredientePorFichaDTO:** DTO de suporte que transporta os dados de um ingrediente específico dentro do contexto de uma ficha (Pesos e Fatores).

#### 3. Refeições

- **RefeicaoDTO:** Utilizado para criar ou editar agrupamentos de refeições.
- **RefeicaoResponseDTO:** Retorna os dados da refeição com o resumo das fichas técnicas vinculadas.
- **RefeicaoNutrientesResponseDTO:** Focado no resumo nutricional total da refeição (Kcal, Proteínas, Carboidratos, etc.).

#### 4. Usuários e Estabelecimentos

- **UsuarioDTO / GetUsuarioDTO:** Gerenciam o fluxo de dados de perfil e registro, ocultando informações sensíveis como a senha na saída.
- **EstabelecimentoDTO / GetEstabelecimentoDTO:** Transportam dados de identificação e listagem de unidades organizacionais.

## Repositories & Specifications

A camada de acesso a dados do Victus utiliza o **Spring Data JPA** para abstrair a complexidade das operações de banco de dados, permitindo um código focado na regra de negócio.

### Repositories (Camada de Persistência)

Cada entidade principal possui seu respectivo repositório, estendendo `JpaRepository` e `JpaSpecificationExecutor`.

- **Principais Repositórios:** `UsuarioRepository`, `FichaTecnicaRepository`, `IngredienteRepository`, `RefeicaoRepository`, `EstabelecimentoRepository`.
- **Recursos Utilizados:**
  - **Derived Queries:** Consultas geradas automaticamente pelo nome do método (ex: `findByEmail`).
  - **Entity Graphs:** Utilizados para resolver o problema do _N+1 SELECT_, carregando relacionamentos complexos de forma eficiente em uma única consulta.
  - **Projeções:** Uso de DTOs nas consultas para retornar apenas os campos necessários.

### Specifications (Consultas Dinâmicas)

Para lidar com filtros de pesquisa avançados e complexos, o sistema implementa o padrão **Specification** (Criteria API do JPA). Isso permite construir consultas dinâmicas de forma programática e segura (Type-safe).

#### 1. FichaTecnicaSpecification

O filtro mais robusto do sistema.

- **Filtros Dinâmicos:** Permite filtrar fichas por Nome, Categoria, Nutricionista, Estabelecimento e valores exatos de nutrientes (VTC, PTN, CHO, etc.).
- **Lógica de Tags (Inteligência de Dados):** Implementa o método `byTag`, que categoriza fichas em **Alta, Média ou Baixa** concentração de nutrientes com base em limites técnicos pré-definidos no código.

#### 2. IngredienteSpecification

Gerencia a busca dinâmica de ingredientes.

- **Filtros:** Busca por nome (ignore case), status e filtros nutricionais.

#### 3. RefeicaoSpecification

Suporta a listagem e filtragem de cardápios.

- **Dinamismo:** Permite filtrar refeições pelo nome e status, facilitando a gestão para o nutricionista.

## Security

A segurança do Victus é implementada utilizando o **Spring Security** com uma arquitetura baseada em **Tokens JWT (JSON Web Tokens)**, garantindo uma autenticação robusta e escalável (stateless).

### Arquitetura de Autenticação

- **Stateless:** O sistema não mantém sessões no servidor. Toda a autenticação é validada através de tokens JWT enviados nas requisições.
- **Custom Authentication Provider:** Utiliza o `CustomAuthenticationProvider` para validar as credenciais do usuário e seu status de ativação antes de conceder o acesso.
- **JwtAuthenticationFilter:** Filtro customizado que intercepta todas as requisições para extrair e validar o token, reconstruindo o contexto de segurança do Spring para o usuário autenticado.

### Autorização e Perfis (Roles)

O acesso às funcionalidades é estritamente controlado com base no cargo do usuário:

- **ROLE_ADMIN:** Acesso a gestão de usuários e estabelecimentos.
- **ROLE_NUTRICIONISTA:** Acesso completo ao módulo de Ingredientes, Fichas Técnicas e Refeições.
- **ROLE_PRODUCAO:** Acesso a visualização de Fichas Técnicas e Refeições.

### Proteções e Configurações

- **Criptografia:** Senhas são protegidas utilizando um algoritmo de hashing seguro.
- **CSRF:** Desabilitado devido ao uso de arquitetura baseada em tokens (stateless).
- **Tratamento de Exceções:**
  - **AuthenticationEntryPoint:** Usuários não autenticados são redirecionados automaticamente para a página de login.
  - **AccessDeniedHandler:** Usuários autenticados que tentam acessar recursos fora de seu cargo são redirecionados para uma página de "Acesso Negado".
- **Method Security:** Utiliza `@EnableMethodSecurity`, permitindo o uso de anotações como `@PreAuthorize` diretamente nos métodos dos serviços para um controle granular.

## Services (Regras de Negócio)

A camada de Service é onde reside a "inteligência" do Victus. Ela é responsável por orquestrar os dados, aplicar validações rigorosas e realizar os cálculos técnicos necessários para a gestão nutricional.

### 1. Gestão de Fichas Técnicas (`FichaTecnicaService`)

Esta é a lógica mais complexa do sistema, envolvendo múltiplos cálculos interdependentes:

- **Cálculo de Rendimento e Porções:** O sistema calcula automaticamente o número de porções baseando-se no rendimento total da preparação dividido pelo peso individual da porção.
- **Engenharia de Custos:** Calcula o **Custo Total** (soma do custo de todos os ingredientes baseados em seu Peso Bruto) e o **Custo Per Capita** (Custo Total / Número de Porções).
- **Validação de Duplicidade:** Garante que o mesmo nutricionista não crie fichas com nomes ou números repetidos, mantendo a organização do acervo.

### 2. Cálculos Nutricionais (`PerfilNutricionalCalculator`)

O sistema realiza uma análise profunda de cada preparação:

- **Proporção de Macronutrientes:** Calcula a quantidade de Proteínas, Carboidratos e Lipídios baseada no **Peso Líquido (PL)** de cada ingrediente.
- **Valor Energético Total (VTC):** Aplica os fatores de conversão (4 kcal para PTN/CHO e 9 kcal para LIP) para chegar à caloria total da porção.
- **Distribuição Percentual:** Calcula quanto cada macronutriente representa no valor calórico total, essencial para o equilíbrio dietético.

### 3. Gestão de Ingredientes e Integração TACO (`IngredienteService`)

- **Isolamento de Dados:** Cada nutricionista possui seu próprio banco de ingredientes privados.
- **Tabela TACO:** O sistema integra uma versão da Tabela Brasileira de Composição de Alimentos (TACO). A regra de negócio impede que um usuário crie um ingrediente privado com o mesmo nome de um ingrediente já existente na tabela oficial, evitando redundância.
- **Controle de Status:** Permite desativar ingredientes sem excluí-los, preservando o histórico de fichas técnicas antigas que ainda os utilizam.

### 4. Planejamento de Refeições (`RefeicaoService`)

- **Consolidação Nutricional:** Ao agrupar diversas fichas técnicas em uma refeição (como um "Almoço"), o sistema soma automaticamente todos os perfis nutricionais para dar ao nutricionista a visão total daquela refeição.
- **Sincronização Dinâmica:** Sempre que uma refeição é editada ou uma ficha vinculada é alterada, os totais da refeição são recalculados instantaneamente.

### 5. Gestão de Usuários e Segurança Operacional (`UsuarioService`)

- **Ativação Controlada:** Novos usuários registrados não possuem acesso imediato. Um administrador deve ativar a conta e atribuir um cargo (`Cargo`) antes que o usuário possa operar o sistema.
- **Gestão de Perfil:** Controla a atualização de dados sensíveis e o armazenamento de imagens de perfil, garantindo a integridade dos arquivos no servidor.

### 6. Importação Automática da Tabela TACO (`TacoInitializationService`)

O Victus já nasce com uma base de dados rica graças ao processo de _Bootstrap_ (inicialização automática):

- **Origem dos Dados:** O sistema processa arquivos Excel oficiais (`Taco.xlsx` e `Acidos.xlsx`) localizados no _classpath_ do projeto.
- **Processamento de Dados:**
  - **Enriquecimento:** O sistema cruza os dados da planilha principal com a planilha de ácidos graxos para garantir que os valores de **Gordura Saturada** sejam importados corretamente, algo que muitas vezes está separado nas fontes originais.
  - **Dono do Sistema:** Todos os ingredientes da TACO são atribuídos a um usuário interno especial (`taco_user`), tornando-os disponíveis para consulta global por todos os nutricionistas, mas protegidos contra edição manual.
- **Execução Inteligente:** A importação ocorre apenas na primeira execução ou quando o banco de dados está vazio, evitando duplicação e garantindo que o sistema esteja pronto para uso imediato após a instalação.

## Qualidade & Testes

O Victus prioriza a estabilidade e a confiabilidade dos cálculos nutricionais. Para isso, conta com uma robusta suíte de testes que cobre as principais funcionalidades do sistema.

### Estrutura da Suíte de Testes

Atualmente, o projeto conta com **128 testes automatizados**, divididos em duas categorias principais:

- **Testes Unitários:** Focados na lógica pura de negócio. Validam os cálculos do `PerfilNutricionalCalculator`, as transformações dos `Mappers` e as validações de senha. Utilizam **Mockito** para isolar as dependências e garantir rapidez na execução.
- **Testes de Integração:** Validam o funcionamento do sistema como um todo. Utilizam o banco de dados **H2 (em memória)** para simular persistência real e o **MockMvc** para testar os endpoints dos controllers, incluindo fluxos de autenticação e permissões de acesso.

### Ferramentas Utilizadas

- **JUnit 5:** Framework base para escrita e execução dos testes.
- **Mockito:** Biblioteca para criação de objetos simulados (Mocks).
- **MockMvc:** Utilizado para simular requisições HTTP e validar o comportamento da camada Web.
- **Spring Security Test:** Ferramenta específica para testar cenários de autenticação, perfis de acesso e proteção CSRF.

### Garantia de Integridade

A suíte de testes é executada a cada alteração significativa no código, garantindo que:

- Cálculos nutricionais permaneçam precisos.
- Regras de segurança (RBAC) impeçam acessos indevidos.
- Fluxos de importação de dados (como a TACO) funcionem corretamente em ambientes limpos.

## Frontend (Arquitetura da Interface)

A interface do Victus foi projetada para ser rápida, responsiva e de fácil manutenção, utilizando tecnologias modernas que permitem uma experiência de Single Page Application (SPA) dentro de um ambiente tradicional de servidor.

### Estrutura de Pastas (`src/main/resources`)

O frontend está organizado de forma modular para facilitar o reuso de código:

#### 1. Templates (Thymeleaf)

Localizados em `src/main/resources/templates`, utilizam o motor de templates **Thymeleaf**:

- **Layout.html:** O template mestre que define a estrutura global da página (Head, Sidebar, Header e rodapé).
- **fragments/:** Contém partes reutilizáveis da interface, como menus de navegação e modais, que são injetados no Layout conforme necessário.
- **pages/:** Agrupa as visualizações completas por módulo (ex: `pages/ficha`, `pages/refeicao`, `pages/usuario`).
- **components/:** Armazena componentes personalizados, como botões, inputs, etc...

#### 2. Arquivos Estáticos (`src/main/resources/static`)

- **Scripts/:** Contém a lógica de comportamento do lado do cliente organizada por módulo (ex: `ScriptFicha.js`, `ScriptRefeicao.js`).
- **input.css:** Arquivo de estilos com configuração do Tailwinde e CSS personalizado.
- **imagens/:** Armazena ícones e recursos visuais estáticos do sistema.

### Tecnologias de Interface

- **Thymeleaf:** Motor de templates que permite a renderização dinâmica de dados do back-end no HTML de forma segura.
- **HTMX:** Utilizado para criar interações dinâmicas (como carregamento de modais e atualizações parciais de listas) sem a necessidade de recarregar a página inteira, proporcionando uma navegação fluida.
- **JavaScript Moderno:** Utilizado para manipulação refinada do DOM e integração com APIs de terceiros.

## Tecnologias & Dependências

O Victus é construído sobre o ecossistema Java moderno, utilizando as bibliotecas mais estáveis e performáticas do mercado.

### Core Stack

- **Java 17:** Versão LTS para máxima estabilidade e recursos de linguagem modernos (como Records).
- **Spring Boot 3.4.5:** Framework base para inversão de controle e autoconfiguração.

### Bibliotecas Principais

- **Spring Data JPA:** Abstração de persistência.
- **MySQL Connector:** Driver para o banco de dados de produção.
- **H2 Database:** Banco de dados em memória para testes rápidos.
- **Spring Security + JJWT (0.11.5):** Camada de segurança e tokens JWT.
- **MapStruct (1.6.3):** Mapeamento de objetos com alta performance.
- **iText (7.2.3):** Geração de relatórios técnicos em PDF.
- **Apache POI (5.4.1):** Processamento de arquivos Excel (Tabela TACO).
- **P6Spy:** Monitoramento e log de queries SQL em tempo de desenvolvimento.

## Comandos Úteis

O projeto utiliza o **Maven Wrapper**, dispensando a necessidade de instalar o Maven globalmente.

### Rodar o Projeto

Para iniciar o servidor de desenvolvimento:

```bash
./mvnw spring-boot:run
```

### Executar Testes

Para rodar toda a suíte de testes unitários e de integração:

```bash
./mvnw test
```

### Build do Projeto

Para limpar e gerar o arquivo .jar de produção:

```bash
./mvnw clean install
```

### Permissão de Execução (Linux/macOS)

Caso o wrapper não tenha permissão de execução:

```bash
chmod +x mvnw
```
