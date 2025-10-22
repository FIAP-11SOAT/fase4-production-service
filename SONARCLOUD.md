# SonarCloud Configuration Guide

## Configuração no SonarCloud

### 1. Acesso ao SonarCloud
1. Acesse [SonarCloud](https://sonarcloud.io)
2. Faça login com sua conta GitHub
3. Importe o repositório `FIAP-11SOAT/fase4-production-service`

### 2. Configuração do Token
1. No SonarCloud, vá em **My Account** > **Security**
2. Gere um novo token com nome `fase4-production-service`
3. Copie o token gerado

### 3. Configuração no GitHub
1. Vá para o repositório no GitHub
2. Acesse **Settings** > **Secrets and variables** > **Actions**
3. Adicione um novo secret:
   - **Name**: `SONAR_TOKEN`
   - **Value**: Cole o token do SonarCloud

### 4. Configuração da Organização
No arquivo `sonar-project.properties`, certifique-se de que:
- `sonar.organization` está configurado corretamente para sua organização
- `sonar.projectKey` corresponde ao nome do projeto no SonarCloud

## Executando Localmente

### Executar análise SonarCloud local:
```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=FIAP-11SOAT_fase4-production-service \
  -Dsonar.organization=fiap-11soat \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=SEU_SONAR_TOKEN
```

### Executar apenas testes com coverage:
```bash
mvn clean test
```

### Gerar relatório de coverage:
```bash
mvn jacoco:report
```

## Métricas Acompanhadas

- **Coverage**: Cobertura de código pelos testes
- **Duplications**: Código duplicado
- **Maintainability**: Maintainability rating
- **Reliability**: Reliability rating  
- **Security**: Security rating
- **Quality Gate**: Status geral da qualidade

## Quality Gate

O projeto está configurado com Quality Gate que verifica:
- Coverage mínima de 80%
- 0 bugs
- 0 vulnerabilidades
- Rating A para maintainability
- Rating A para reliability
- Rating A para security

## Branches Analisadas

- `main`: Branch principal
- `develop`: Branch de desenvolvimento
- Pull Requests: Análise automática em PRs