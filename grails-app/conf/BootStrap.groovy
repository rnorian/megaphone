import com.norian.megaphone.domain.Contact
import com.norian.megaphone.domain.ContactList

class BootStrap {

    def init = { servletContext ->
        def raffi = new Contact(alias: "rn", firstName: "Raffi", lastName: "Norian", phoneNumber: "775-830-7051").save(flush: true, failOnError: true)
        def luciana = new Contact(alias: "ln", firstName: "Luciana", lastName: "Norian", phoneNumber: "775-345-5158").save(flush: true, failOnError: true)
        def list = new ContactList(name: "real-practice", alias: "real-practice").save(flush: true, failOnError: true)
        list.addToTargets(raffi)
        list.addToTargets(luciana)
        list.save(flush: true, failOnError: true)

    }

    def destroy = {
    }
}
