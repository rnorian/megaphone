package com.norian.megaphone.domain

abstract class MessageTarget {

    String alias;

    static constraints = {
    }

    abstract SendResult sendMessage(String pMessage);
}
