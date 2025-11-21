# FiadoPay Backend (Commits 1–5)

Projeto Spring Boot com H2 que implementa pagamentos, idempotência, anotações customizadas e antifraude.

## Requisitos
- Java 21 instalado (`java -version`)
- Windows PowerShell (exemplos usam PowerShell)
- Não precisa instalar Maven: usa `mvnw.cmd`

## Como rodar os testes
```
cd backend
.\mvnw.cmd -q test
```

## Como executar a aplicação
```
cd backend
.\mvnw.cmd spring-boot:run
```
- Porta padrão: `8080`. Se ocupada, use:
  `.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"`

## Banco de dados H2
- Console: `http://localhost:8080/h2`
- JDBC URL: `jdbc:h2:mem:fiadopay`
- Usuário: `sa`
- Senha: (vazia)

## Endpoints e cabeçalhos
- `POST /payments`
  - Headers obrigatórios:
    - `Authorization: Bearer <qualquer>` (auth fake)
    - `X-Idempotency-Key: <UUID ou string única>`
  - Body (JSON):
    `{ "amount": 100, "currency": "BRL", "method": "CARD", "installments": 1 }`

- `GET /payments/{id}` (Authorization obrigatório)
- `GET /payments` (Authorization obrigatório)

## Exemplos (PowerShell)
### Invoke-RestMethod (recomendado)
```
$body = @{ amount = 100; currency = 'BRL'; method = 'CARD'; installments = 1 } | ConvertTo-Json -Compress
Invoke-RestMethod -Uri 'http://localhost:8080/payments' -Method Post -Headers @{ Authorization = 'Bearer x'; 'X-Idempotency-Key' = [guid]::NewGuid().ToString() } -ContentType 'application/json' -Body $body
```

### curl.exe
```
curl.exe -X POST "http://localhost:8080/payments" -H "Authorization: Bearer x" -H "X-Idempotency-Key: 123e4567-e89b-12d3-a456-426614174000" -H "Content-Type: application/json" --data-raw "{\"amount\":100,\"currency\":\"BRL\",\"method\":\"CARD\",\"installments\":1}"
```

## Demonstração das funcionalidades
### Commit 1 — Base de domínio e H2
- Entidade `Payment` com campos: id, amount, currency, method, installments, interestRate, totalAmount, status, idempotencyKey, createdAt, updatedAt (`backend/src/main/java/com/fiadopay/backend/entity/Payment.java:1`).
- Enums `PaymentStatus` (`PENDING, APPROVED, DECLINED, SETTLED`) e `PaymentMethodType`.
- Repositório `PaymentRepository` com `findByIdempotencyKey`.
- H2 configurado, logs SQL formatados.

### Commit 2 — DTOs, Controller e Auth Fake
- DTOs: `PaymentRequest` (Bean Validation), `PaymentResponse`.
- Endpoints: `POST /payments`, `GET /payments/{id}`, `GET /payments` (`backend/src/main/java/com/fiadopay/backend/controller/PaymentController.java:1`).
- Interceptor de auth fake exige `Authorization: Bearer ...` (libera `/h2`).

### Commit 3 — Idempotência
- `POST /payments` exige `X-Idempotency-Key` e retorna o mesmo pagamento para a mesma chave.
- Constraint única em `Payment.idempotencyKey`.

### Commit 4 — Anotações e Scanner
- `@PaymentMethod`, `@AntiFraud`, `@WebhookSink` (Retention RUNTIME, Target TYPE).
- `AnnotationRegistry` usa Reflections para registrar handlers e regras jar-friendly.

### Commit 5 — Antifraude HighAmount
- Regra `HighAmountRule` reprova quando `amount > threshold` e define `status=DECLINED` com `declineReason`.
- `PaymentService.processPayment` executa todas as regras registradas.

## Como validar manualmente
1. Criar pagamento válido:
   - `amount=100`, `currency=BRL`, `method=CARD`, `installments=1`  → `status=PENDING`.
2. Idempotência:
   - Envie o mesmo `X-Idempotency-Key` duas vezes → mesmo `id`.
3. Antifraude:
   - `amount=1200` → `status=DECLINED` e `declineReason` contendo `HighAmount`.
4. Listar e buscar:
   - `GET /payments`
   - `GET /payments/{id}`

## Executar build
```
cd backend
.\mvnw.cmd -q -DskipTests clean package
```

## Observações
- No PowerShell, `curl` pode ser alias de `Invoke-WebRequest`. Use `curl.exe` ou `Invoke-RestMethod` como nos exemplos.
- Se `8080` estiver ocupada, execute em `8081` com o parâmetro informado acima.
- H2 Console em `/h2` não exige `Authorization`.