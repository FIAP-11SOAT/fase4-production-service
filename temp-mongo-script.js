// Simular mudança de status e publicação
db.productions.updateOne(
    {orderId: "2001"}, 
    {$set: {status: "IN_PROGRESS", updatedAt: new Date()}}
);

// Mostrar resultado da mudança
var updated = db.productions.findOne({orderId: "2001"});
print("Registro atualizado:");
printjson(updated);

// Contagem de status após mudança
print("\nContagem por status:");
print("PENDING: " + db.productions.countDocuments({status: "PENDING"}));
print("IN_PROGRESS: " + db.productions.countDocuments({status: "IN_PROGRESS"}));
print("DONE: " + db.productions.countDocuments({status: "DONE"}));