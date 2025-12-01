## 🎤 KaraQ: Sistema de Gerenciamento de Fila de Karaokê 🎶

-----

## 📌 Sobre o Projeto

O KaraQ [nome provisório] consiste em um **sistema de gerenciamento de sessões de karaokê**, desenvolvido em **Java** e fundamentado nos princípios da Programação Orientada a Objetos (POO). O objetivo principal é simplificar e automatizar a organização de eventos de karaokê, oferecendo uma solução completa que abrange desde a criação de uma sessão até o controle da fila de músicas em tempo real. A aplicação permite que um anfitrião (**Host**) inicie e modere o evento, enquanto os participantes podem entrar na sessão, adicionar suas músicas preferidas e visualizar a ordem das apresentações, garantindo uma experiência fluida e organizada para todos.

-----

## 🔧 Tecnologias Utilizadas

  * **Back-end:** Java (com **Spring Boot**, **Security**, **Web**, **JPA**, **Web Socket**)
  * **Front-end:** Angular
  * **Banco de Dados:** PostgreSQL
  * **Containerização:** Docker e Docker Compose

-----

## 🚀 Como Rodar o Projeto Localmente

Para iniciar e rodar a aplicação KaraQ localmente, você precisará ter o **Docker** e o **Docker Compose** instalados em sua máquina. O Docker Compose será usado para gerenciar a execução do banco de dados PostgreSQL.

### Pré-requisitos

1.  **Docker** instalado e em execução.
2.  **Docker Compose** instalado.
3.  **JDK 21** ou superior instalado (para o *backend*).
4.  **Maven** instalado (para o *backend*).
5.  **Node.js** e **npm** instalados (para o *frontend* Angular).
6.  **Angular CLI** instalado globalmente (`npm install -g @angular/cli`).

### Passos de Execução

#### 1\. Iniciar o Banco de Dados com Docker Compose

Navegue até o diretório **backend** do projeto (onde está o arquivo `docker-compose.yml`) e execute o seguinte comando no seu terminal:

```bash
docker-compose up -d
```

  * Este comando irá construir e iniciar o contêiner do **postgresql** em *background*.
  * O banco de dados estará acessível pelo *backend* Java.

#### 2\. Iniciar o Back-end (Java/Spring Boot)

Navegue até o diretório do *backend* (`/backend` ou o nome da pasta que contém o `pom.xml`):

```bash
cd backend
```

Execute o *backend* usando o Maven:

```bash
mvn spring-boot:run
```

  * O servidor *backend* deve iniciar, por padrão, na porta **8080** (verifique a configuração do `application.properties` ou `application.yml`).

#### 3\. Iniciar o Front-end (Angular)

Abra um **novo terminal** e navegue até o diretório do *frontend* (`/frontend` ou o nome da pasta que contém o `package.json`):

```bash
cd ../frontend
```

Execute o *frontend* usando o NPM:

```bash
npm start
```

  * O servidor de desenvolvimento do *frontend* deve iniciar, por padrão, na porta **4200**.
  * Você pode acessar a aplicação no seu navegador em `http://localhost:4200/`.

-----

## 🗂️ Entregas

<details>
<summary>Entrega 01</summary>

### Histórias de usuários

  - Documento: [https://docs.google.com/document/d/1IFvm73vHbsO3gl7l72AiBhigJqb5kcNGL9uPeIZxxqs/edit?usp=sharing](https://docs.google.com/document/d/1IFvm73vHbsO3gl7l72AiBhigJqb5kcNGL9uPeIZxxqs/edit?usp=sharing)

### Protótipo Lo-Fi

  - Link protótipo figma: [https://www.figma.com/design/LyXUdIbouOKrxDKlmoEArr/Prot%C3%B3tipo-Lo-fi--KaraQ-?node-id=0-1\&t=EWGiLF5bN2I0hstu-1](https://www.figma.com/design/LyXUdIbouOKrxDKlmoEArr/Prot%C3%B3tipo-Lo-fi--KaraQ-?node-id=0-1&t=EWGiLF5bN2I0hstu-1)
  - Link Screencast youtube: [https://youtu.be/6vqRQePJMvc](https://youtu.be/6vqRQePJMvc)
  - Link Screencast alternativo (Drive): [https://drive.google.com/file/d/1CZbQ0-OU7feeF81PJM8lqcsiI8qBAj1g/view?usp=sharing](https://drive.google.com/file/d/1CZbQ0-OU7feeF81PJM8lqcsiI8qBAj1g/view?usp=sharing)
  - [link suspeito removido]

</details>

<details>
<summary>Entrega 02</summary>

### 2 Funcionalidades Implementadas

  - Link Screencast youtube: [https://youtu.be/EmzqTwur95k](https://youtu.be/EmzqTwur95k)
  - Issue/Bug Tracker 1 (Github): [https://drive.google.com/file/d/1ifb4oiXXE2wI4W\_kj5F0w\_hyvJO1u2Hs/view?usp=drive\_link](https://drive.google.com/file/d/1ifb4oiXXE2wI4W_kj5F0w_hyvJO1u2Hs/view?usp=drive_link)
  - Issue/Bug Tracker 2 (Github): [https://drive.google.com/file/d/1tpihUOwz2knCuZ8nn8REKV\_hbypas1Ug/view?usp=drive\_link](https://drive.google.com/file/d/1tpihUOwz2knCuZ8nn8REKV_hbypas1Ug/view?usp=drive_link)

</details>

<details>
<summary>Entrega 03</summary>

### 2 Funcionalidades Implementadas

  - Link Screencast youtube: [https://youtu.be/BpuKaSfmyWg](https://youtu.be/BpuKaSfmyWg)
  - Issue/Bug Tracker Atualizado (Github): [https://drive.google.com/file/d/1FcEu4tHqywIjxYG2A8r8JNVD5Iw6EGFs/view?usp=drive\_link](https://drive.google.com/file/d/1FcEu4tHqywIjxYG2A8r8JNVD5Iw6EGFs/view?usp=drive_link)
  - Issue/Bug Tracker 1 (Github): [https://drive.google.com/file/d/1B9JuDusNKd7kSfkoHsTepYiqsvR18H4d/view?usp=drive\_link](https://drive.google.com/file/d/1B9JuDusNKd7kSfkoHsTepYiqsvR18H4d/view?usp=drive_link)
  - Issue/Bug Tracker 2 (Github): [https://drive.google.com/file/d/1A-hmoPIZD7xNBV-Pg12gNTZ\_bvos0eL4/view?usp=drive\_link](https://drive.google.com/file/d/1A-hmoPIZD7xNBV-Pg12gNTZ_bvos0eL4/view?usp=drive_link)
  - Issue/Bug Tracker 3 (Github): [https://drive.google.com/file/d/1wqXvT-nMVHGA794uk7dlkVUNQganG7gb/view?usp=drive\_link](https://www.google.com/search?q=https://drive.google.com/file/d/1wqXvT-nMVHGA794uk7dlkVUNQganG7gb/view%3Fusp%3Ddrive_link)
  - Issue/Bug Tracker 4 (Github): [https://drive.google.com/file/d/1QXE0hLCvWANXpGN-0nuIae9KJoYnY75C/view?usp=drive\_link](https://drive.google.com/file/d/1QXE0hLCvWANXpGN-0nuIae9KJoYnY75C/view?usp=drive_link)
  - Link Screencast Testes Youtube: [https://youtu.be/Nm\_NFuRtC1s](https://youtu.be/Nm_NFuRtC1s)
  - Link Screencast Testes Alternativo (Drive): [https://drive.google.com/file/d/1kxfHKaIiGUBHdnxwXYX9sX6ZoagGvrvS/view?usp=drive\_link](https://drive.google.com/file/d/1kxfHKaIiGUBHdnxwXYX9sX6ZoagGvrvS/view?usp=drive_link)

</details>

<details>
<summary>Entrega 04</summary>

### 3 Funcionalidades Implementadas

  - Link Screencast youtube: [https://youtu.be/LFT6EQ84cEU](https://youtu.be/LFT6EQ84cEU)
  - Issue/Bug Tracker 1 (Github): [https://drive.google.com/drive/u/0/folders/1U6ZqkiD2ooi9pO8envf3r33XOlHdvZJf](https://drive.google.com/drive/u/0/folders/1U6ZqkiD2ooi9pO8envf3r33XOlHdvZJf)
  - Issue/Bug Tracker 2 (Github): [https://drive.google.com/drive/u/0/folders/1U6ZqkiD2ooi9pO8envf3r33XOlHdvZJf](https://drive.google.com/drive/u/0/folders/1U6ZqkiD2ooi9pO8envf3r33XOlHdvZJf)
  - Issue/Bug Tracker 3 (Github): [https://drive.google.com/drive/u/0/folders/1U6ZqkiD2ooi9pO8envf3r33XOlHdvZJf](https://drive.google.com/drive/u/0/folders/1U6ZqkiD2ooi9pO8envf3r33XOlHdvZJf)
  - Link Screencast Testes Youtube: [https://youtu.be/nV07ow5Mlg4](https://youtu.be/nV07ow5Mlg4)

</details>

-----

## Equipe 4 - Kant

  - Ana Sofia - [@Sun-cs-Sol](https://github.com/Sun-cs-Sol) - [Linkedin](https://www.linkedin.com/in/ana-sofia-moura-27b003248/)
  - Camila Maria - [@camilamta275](https://github.com/camilamta275) - [Linkedin](https://www.linkedin.com/in/camilamta275/)
  - Lucas Rodrigues - [@lucxsz-web](https://github.com/lucxsz-web) - [Linkedin](https://www.linkedin.com/in/lucas-rodrigues-08261b2ba/)
  - René Melo - [@renysoo](https://github.com/renysoo) - [Linkedin](https://www.linkedin.com/in/renelucena/)
  - Victor Ferreira - [@vic-fmr](https://github.com/vic-fmr) - [Linkedin](https://www.linkedin.com/in/victor-ferreira-marques/)


