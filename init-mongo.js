// Inicialização do MongoDB
db = db.getSiblingDB('productiondb');

// Criar usuário para a aplicação
db.createUser({
  user: 'app_user',
  pwd: 'app_password',
  roles: [
    {
      role: 'readWrite',
      db: 'productiondb'
    }
  ]
});

// Criar índices para performance
db.productions.createIndex({ "orderId": 1 }, { unique: true });
db.productions.createIndex({ "status": 1 });
db.productions.createIndex({ "startedAt": 1 });

// Inserir dados de exemplo para teste
db.productions.insertMany([
  {
    "orderId": NumberLong(1001),
    "productIds": [NumberLong(501), NumberLong(502)],
    "status": "PREPARING",
    "startedAt": new Date(),
    "createdAt": new Date(),
    "updatedAt": new Date()
  },
  {
    "orderId": NumberLong(1002),
    "productIds": [NumberLong(503)],
    "status": "IN_PROGRESS",
    "startedAt": new Date(Date.now() - 3600000), // 1 hora atrás
    "createdAt": new Date(Date.now() - 3600000),
    "updatedAt": new Date()
  },
  {
    "orderId": NumberLong(1003),
    "productIds": [NumberLong(504), NumberLong(505), NumberLong(506)],
    "status": "DONE",
    "startedAt": new Date(Date.now() - 7200000), // 2 horas atrás
    "completedAt": new Date(Date.now() - 1800000), // 30 min atrás
    "createdAt": new Date(Date.now() - 7200000),
    "updatedAt": new Date(Date.now() - 1800000)
  }
]);

print("MongoDB inicializado com dados de exemplo!");