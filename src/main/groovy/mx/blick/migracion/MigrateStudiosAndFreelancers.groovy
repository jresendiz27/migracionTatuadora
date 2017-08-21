package mx.blick.migracion

import groovy.util.logging.Log4j

@Log4j
class MigrateStudiosAndFreelancers {
    static void main(def args) {
        DatabaseTool databaseTool = DatabaseTool.instance
        CSVReader csvReader = CSVReader.instance

        csvReader.loadStudiosAndFreelancers()

        List<Map> studios = csvReader.studios
        List<Map> freelancers = csvReader.freelancers

        databaseTool.truncateUser();
        databaseTool.truncateStudio();
        databaseTool.truncateFreelance()
        databaseTool.truncateAddress()

        studios.each { studioMap ->
            Long createdAddressId = databaseTool.createAddress(studioMap).first()[0] as Long
            Long createdUserId = databaseTool.insertUserStudio(studioMap).first()[0] as Long
            studioMap.addressId = createdAddressId
            databaseTool.createStudio(createdUserId, studioMap)
        }

        freelancers.each { freelancerMap ->
            Long createdAddressId = databaseTool.createAddress(freelancerMap).first()[0] as Long
            Long createdUserId = databaseTool.insertUserFreelance(freelancerMap).first()[0] as Long
            freelancerMap.addressId = createdAddressId
            databaseTool.createFreelance(createdUserId, freelancerMap)
        }

    }
}
