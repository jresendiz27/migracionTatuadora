package mx.blick.migracion

import groovy.util.logging.Log4j

@Log4j
class MigrateImages {
    static void main(def args) {
        DatabaseTool databaseConnections = DatabaseTool.getInstance()
        databaseConnections.retrieveDatabaseUsers().each { wpUser ->
            log.error(">> ${wpUser.toString()} ")
            for(meta in wpUser.userMeta) {
                log.error(" -- ${meta.toString()}")
            }

        }
    }
}
