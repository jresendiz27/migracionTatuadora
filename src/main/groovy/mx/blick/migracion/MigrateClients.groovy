package mx.blick.migracion

import groovy.util.logging.Log4j

@Log4j
class MigrateClients {
    static void main(def args) {
        DatabaseTool databaseTool = DatabaseTool.instance
        CSVReader csvReader = CSVReader.instance
        csvReader.loadClients()
        List<Map> clients = csvReader.clients

        clients.each { clientMap ->
            Long createdAddressId = databaseTool.createAddress(clientMap).first()[0] as Long
            clientMap.addressId = createdAddressId
            databaseTool.insertUserClient(clientMap)
        }
    }
}
