# Micro Rede Social - IFSP

Este projeto foi desenvolvido como parte da disciplina de Desenvolvimento de Sistemas para Dispositivos Móveis no **IFSP (Instituto Federal de São Paulo)**. O objetivo foi criar uma rede social funcional, utilizando tecnologias modernas de desenvolvimento Android e integração com serviços em nuvem.

## Vídeos do Projeto

Para entender melhor o funcionamento e a implementação técnica, assista aos vídeos abaixo:

*   **Demonstração de Uso (Short):** [Assista aqui](https://youtube.com/shorts/Lf0swMmpE0I) - *Uma visão rápida do app funcionando.*
*   **Explicação Detalhada do Código:** [Assista aqui](https://youtu.be/V6-RnCx6Spg) - *Mergulho técnico nas Activities, UI e lógica de paginação.*

---

## Funcionalidades Principais

*   **Sistema de Login e Cadastro:** Autenticação segura via Firebase Auth.
*   **Feed com Paginação:** As postagens são carregadas de 5 em 5 para otimizar o consumo de dados e performance (RF3-1).
*   **Geolocalização (GPS):** Identificação automática da cidade do usuário no momento da postagem usando `FusedLocationProvider`.
*   **Busca por Cidade:** Filtro dinâmico no Firestore para encontrar postagens de locais específicos.
*   **Perfil Personalizável:** Edição de dados pessoais e foto de perfil (convertida em Base64 para persistência leve).
*   **Design Responsivo:** Interface construída com `CoordinatorLayout` e `BottomAppBar`, garantindo fluidez e usabilidade.

## Aspectos Técnicos

*   **Linguagem:** Kotlin
*   **Backend:** Firebase (Authentication e Firestore)
*   **UI/UX:** XML Layouts, Material Design 3, ViewBinding e RecyclerView.
*   **Localização:** Google Play Services (Location & Geocoder).

## Estrutura do Código

O projeto está organizado seguindo as melhores práticas de desenvolvimento Android:
*   `ui/`: Activities que controlam a lógica de cada tela.
*   `adapter/`: Gerenciamento da listagem do feed.
*   `model/`: Definição das classes de dados (Post e User).
*   `util/`: Classes auxiliares como o conversor de imagens.

---
**Desenvolvido por:** Matheus Costa Teixeira
**Orientação:** Prof. Henrique Galati
**Instituição:** IFSP - Campus Araraquara
