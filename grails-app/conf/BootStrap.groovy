import com.norian.megaphone.domain.Contact

class BootStrap {

    def init = { servletContext ->
        new Contact(firstName: "Raffi", lastName: "Norian", phoneNumber: "775-830-7051").save(flush: true, failOnError: true)
        new Contact(firstName: "Luciana", lastName: "Norian", phoneNumber: "775-345-5158").save(flush: true, failOnError: true)
    }

    def destroy = {
    }
}
