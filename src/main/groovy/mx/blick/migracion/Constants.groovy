package mx.blick.migracion

class Constants {
    final static Map USER_TYPE = [admin: 1, user: 2, studio: 3, freelance: 4, quotient: 5];
    final static Map STUDIO_STATUS = [
            'pending'  : 1,
            'publicate': 2,
            'block'    : 3
    ]

    final static List USER_PROPERTIES = [
            "name",
            "lastname",
            "email",
            "password",
            "telephone",
            "userType",
            "createdAt",
            "updatedAt"
    ]
    final static List STUDIO_PROPERTIES = [
            "name",
            "certCofepris",
            "addressId",
            "publication",
            "titleImgUrl",
            "logoUrl",
            "profileImgUrl",
            "about",
            "userId",
            "status",
            "membership",
            "membershipExp",
            "id",
            "createdAt",
            "updatedAt"
    ]


    final static List FREELANCE_PROPERTIES = [
            "user",
            "membershipExp",
            "about",
            "published",
            "profileImgUrl",
            "canGoHome",
            "membership",
            "name",
            "rank",
            "createdAt",
            "updatedAt",
    ]

    final static List UNIQUE_STYLES = [
            'Abstracto',
            'Acuarela',
            'Animal',
            'Asiático',
            'Biomecánica',
            'Black Work',
            'Caligrafia',
            'Cartoon',
            'Celta',
            'Dot Work',
            'Fantasía',
            'Figurativo',
            'Geométrico',
            'Gótico',
            'Horror',
            'Ilustracion',
            'Insecto',
            'Japonés',
            'Latino',
            'Lorem Ipsum',
            'New School',
            'Old School',
            'Ornamental',
            'Polinesio',
            'Puntillismo',
            'Realismo',
            'Religioso',
            'Skull',
            'Surreal',
            'Trash Polka',
            'Tribal']
}
