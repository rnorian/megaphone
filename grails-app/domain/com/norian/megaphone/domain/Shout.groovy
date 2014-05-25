package com.norian.megaphone.domain

import java.text.DateFormat


class Shout {

    String messageSid;
    String message;

    Date receivedOn;

    static hasOne = [sender: ApprovedSender];
    static hasMany = [to: MessageTarget, sendResults: SendResult];

    static constraints = {
    }

    void send() {
        for (MessageTarget target in to) {
            SendResult result = target.sendMessage(message);
            result.save();
        }
    }

    @Override
    String toString() {
        return 'sid: ' + messageSid
                + '\nreceivdOn: ' + DateFormat.getDateTimeInstance().format(receivedOn)
                + '\nsender: ' + sender.getName()
                + '\ntarget: ' + to.toListString()
                + '\nmessage: ' + message
    }
}
