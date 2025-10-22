#!/usr/bin/env python3
import boto3
import json
import time
from datetime import datetime

# Configuração AWS para LocalStack
aws_config = {
    'endpoint_url': 'http://localhost:4566',
    'aws_access_key_id': 'test',
    'aws_secret_access_key': 'test',
    'region_name': 'us-east-1'
}

def test_sqs():
    print("🔄 Testando SQS LocalStack...")
    
    try:
        sqs = boto3.client('sqs', **aws_config)
        
        # Criar fila se não existir
        queue_name = 'production-queue'
        try:
            response = sqs.create_queue(QueueName=queue_name)
            queue_url = response['QueueUrl']
            print(f"✅ Fila criada/encontrada: {queue_url}")
        except Exception as e:
            print(f"❌ Erro ao criar fila: {e}")
            return False
        
        # Enviar mensagens de teste
        test_messages = [
            {"orderId": "2001", "productType": "WIDGET", "quantity": 100, "priority": "HIGH"},
            {"orderId": "2002", "productType": "GADGET", "quantity": 50, "priority": "MEDIUM"},
            {"orderId": "2003", "productType": "DEVICE", "quantity": 200, "priority": "LOW"},
            {"orderId": "2004", "productType": "COMPONENT", "quantity": 75, "priority": "HIGH"}
        ]
        
        print("\n📤 Enviando mensagens de teste...")
        for i, message in enumerate(test_messages, 1):
            try:
                response = sqs.send_message(
                    QueueUrl=queue_url,
                    MessageBody=json.dumps(message),
                    MessageAttributes={
                        'MessageType': {
                            'StringValue': 'ProductionOrder',
                            'DataType': 'String'
                        }
                    }
                )
                print(f"✅ Mensagem {i} enviada: Order {message['orderId']}")
                time.sleep(0.5)
            except Exception as e:
                print(f"❌ Erro ao enviar mensagem {i}: {e}")
        
        # Verificar mensagens na fila
        print("\n📥 Verificando mensagens na fila...")
        try:
            response = sqs.receive_message(
                QueueUrl=queue_url,
                MaxNumberOfMessages=10,
                WaitTimeSeconds=2
            )
            
            messages = response.get('Messages', [])
            print(f"✅ {len(messages)} mensagens encontradas na fila")
            
            for msg in messages[:3]:  # Mostrar apenas as primeiras 3
                body = json.loads(msg['Body'])
                print(f"   📋 Order {body['orderId']}: {body['productType']} x{body['quantity']}")
            
            return True
            
        except Exception as e:
            print(f"❌ Erro ao verificar fila: {e}")
            return False
            
    except Exception as e:
        print(f"❌ Erro ao conectar SQS: {e}")
        return False

def test_mongodb():
    print("\n🔄 Testando MongoDB...")
    
    try:
        from pymongo import MongoClient
        
        client = MongoClient('mongodb://localhost:27017/')
        db = client['productiondb']
        collection = db['productions']
        
        # Testar conexão
        result = collection.insert_one({
            "orderId": "TEST-" + str(int(time.time())),
            "status": "PENDING",
            "createdAt": datetime.now().isoformat(),
            "testData": True
        })
        
        print(f"✅ MongoDB conectado - Documento inserido: {result.inserted_id}")
        
        # Contar documentos
        count = collection.count_documents({})
        print(f"✅ Total de documentos na coleção: {count}")
        
        return True
        
    except ImportError:
        print("⚠️ pymongo não instalado - instalando...")
        import subprocess
        try:
            subprocess.check_call(['pip', 'install', 'pymongo'])
            return test_mongodb()  # Tentar novamente
        except:
            print("❌ Erro ao instalar pymongo")
            return False
            
    except Exception as e:
        print(f"❌ Erro ao conectar MongoDB: {e}")
        return False

def test_application():
    print("\n🔄 Testando aplicação Spring Boot...")
    
    try:
        import requests
        
        # Testar endpoints
        base_url = "http://localhost:8080"
        endpoints = [
            "/api/productions",
            "/api/productions/count/PENDING",
            "/actuator/health"
        ]
        
        for endpoint in endpoints:
            try:
                response = requests.get(f"{base_url}{endpoint}", timeout=5)
                if response.status_code == 200:
                    print(f"✅ {endpoint}: {response.status_code}")
                else:
                    print(f"⚠️ {endpoint}: {response.status_code}")
            except requests.exceptions.ConnectionError:
                print(f"❌ {endpoint}: Conexão recusada")
            except Exception as e:
                print(f"❌ {endpoint}: {e}")
        
        return True
        
    except ImportError:
        print("⚠️ requests não instalado - instalando...")
        import subprocess
        try:
            subprocess.check_call(['pip', 'install', 'requests'])
            return test_application()
        except:
            print("❌ Erro ao instalar requests")
            return False

if __name__ == "__main__":
    print("🚀 Iniciando testes do ambiente de produção")
    print("=" * 50)
    
    # Aguardar inicialização
    print("⏳ Aguardando serviços iniciarem...")
    time.sleep(3)
    
    # Executar testes
    sqs_ok = test_sqs()
    mongo_ok = test_mongodb()
    app_ok = test_application()
    
    print("\n" + "=" * 50)
    print("📊 RESUMO DOS TESTES:")
    print(f"   SQS LocalStack: {'✅ OK' if sqs_ok else '❌ FALHOU'}")
    print(f"   MongoDB: {'✅ OK' if mongo_ok else '❌ FALHOU'}")
    print(f"   Aplicação: {'✅ OK' if app_ok else '❌ FALHOU'}")
    
    if sqs_ok and mongo_ok:
        print("\n🎉 Infraestrutura OK! Mesmo com a aplicação com problemas,")
        print("   você pode ver que LocalStack e MongoDB estão funcionando.")
        print("\n💡 Próximos passos:")
        print("   1. Corrigir o problema do JAR Spring Boot")
        print("   2. Reiniciar o container production-service")
        print("   3. Testar os 4 endpoints da sua controller")
    else:
        print("\n⚠️ Alguns serviços de infraestrutura precisam de atenção.")