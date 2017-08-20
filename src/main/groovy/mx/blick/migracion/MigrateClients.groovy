package mx.blick.migracion

import groovy.util.logging.Log4j

@Log4j
class MigrateClients {
    static void main(def args) {
        DatabaseTool databaseTool = DatabaseTool.instance
        CSVReader csvReader = CSVReader.instance
        csvReader.loadClients()
        List<Map> clients = csvReader.clients

        databaseTool.truncateUser();

        clients.each { clientMap ->
            //log.error(clientMap.toString())
            databaseTool.insertClient(clientMap)
        }
    }
}
