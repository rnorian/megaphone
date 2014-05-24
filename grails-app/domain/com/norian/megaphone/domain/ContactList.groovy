package com.norian.megaphone.domain

import grails.rest.*

@Resource(uri='/contactLists', formats = ['json', 'xml'])
class ContactList extends MessageTarget {
    String name

    Set targets = []

    static hasMany = [targets: MessageTarget]

    static constraints = {
    }

    @Override
    SendResult sendMessage(String pMessage) {
        for (MessageTarget target in targets) {
            target.sendMessage(pMessage)
        }
    }
}
