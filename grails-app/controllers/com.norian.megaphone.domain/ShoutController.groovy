package com.norian.megaphone.domain

import grails.rest.*

import com.norian.megaphone.domain.ApprovedSender
import com.norian.megaphone.domain.Shout
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import java.util.regex.Matcher
import java.util.regex.Pattern

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK


/**
 * Shout Message Format
 * TO:<message-target>[,<message-target>]
 * AT:<time> | <date-time>
 * MSG:<text>$ | TEMPLATE:<template-name>
 *
 * defaults:
 * <to> first line of text
 * <at> send now
 * <msg> all lines after first line
 *
 *
 */
class ShoutController extends RestfulController {

    static responseFormats = ['json', 'xml']
    static Pattern toTargetPattern = ~/^(?i)(TO:)?+(.*)$?/
    static Pattern atPattern = ~/^(?i)(AT:)?+(.*)$?/
    static Pattern msgPattern = ~/^(?i)(MSG:)?+(.*)$?/

    //static Pattern toTargetPattern = Pattern.compile('^(TO:)?+(.*)$?', Pattern.CASE_INSENSITIVE)

    ShoutController() {
        super(Shout)
    }

    def index(Integer max) {
        log.error("GET shouts")
        params.max = Math.min(max ?: 10, 100)
        respond Shout.list(params), [status: OK]
    }

    List<String> validateParms(GrailsParameterMap params) {
        def errors = new ArrayList<String>();

        // Twilio passed data of received text
        if (params.MessageSid == null) {
            errors.add('no Twilio MessageSid found')
        }

        if (params.From == null) {
            errors.add('no Twilio From specified')
        } else {
            def approvedSender = ApprovedSender.findByPhoneNumber(params.From)
            if (approvedSender == null) {
                errors.add('sender (' + params.From + ') not approved');
                def contact = Contact.findByPhoneNumber(params.From)
                if (contact != null) {
                    errors.add('-- sender is ' + contact)
                }
            }
        }

        if (params.Body == null) {
            errors.add('no Twilio Body specified')
        }

        return errors;
    }

    def List<String> validateMessage(String msg) {
        def errors = new ArrayList<String>()

        def sections = msg.split('\n')
        if (sections.size() < 2) {
            errors.add 'no discernible message parts (TO:, MSG:) - ignored'
            return errors
        }

        def parsers = ['TO': toTargetPattern, 'AT': atPattern, 'MSG': msgPattern]

        // run each parser over each section of input; if a match is found store it in the sectionMap
        def sectionMap = [:]
        parsers.each {k, pattern ->
            sections.each { s ->
                def matcher = s =~ pattern
                if (matcher.matches()) { sectionMap[k] = s =~ matcher[0][1] }
            }
        }


        def toSection = sections[0]
        Matcher matcher = toTargetPattern.matcher(toSection)
        if (!matcher.matches() || matcher.groupCount() < 2) {
            log.info('could not find TO pattern - ignoring message')
            return;
        }

        def msgTargets = matcher.group(2).split(",")
        msgTargets.each {
            def targets = ContactSearch.search(it)

            if (targets.size() == 1) {
                message.addToTo(targets[0])
            } else if (targets.size() > 1) {
                errors.add('multiple contacts found: ' + it + '\n')
            }
            else {
                errors.add('unknown contact: ' + it + '\n')
            }
        }
        if (message.to.size() == 0) {
            log.info('no message targets')
            render 'no message targets found'
            return
        }
    }

    def save() {
        log.info "new message received $params"

        def errors = validateParms(params)
        if (errors.size()) {
            def errorStr = errors.join("\n")
            log.info("invalid request received $errorStr")
            respond errorStr, status: BAD_REQUEST
            return
        }

        errors = validateMessage(params.Body)

        Shout message = new Shout()
        message.messageSid = params.MessageSid
        message.sender = ApprovedSender.findByPhoneNumber(params.From)
        message.message = params.Body


        def sections = msg.split('\n')
        if (sections.size() < 2) {
            log.info('no discernible message parts (target, body) - ignored')
            render 'Missing body parts (target, message)'
            return;
        }

        def toSection = sections[0]
        Matcher matcher = toTargetPattern.matcher(toSection)
        if (!matcher.matches() || matcher.groupCount() < 2) {
            log.info('could not find TO pattern - ignoring message')
            return;
        }

        def msgTargets = matcher.group(2).split(",")
        msgTargets.each {
            def targets = ContactSearch.search(it)

            if (targets.size() == 1) {
                message.addToTo(targets[0])
            } else if (targets.size() > 1) {
                errors.add('multiple contacts found: ' + it + '\n')
            }
            else {
                errors.add('unknown contact: ' + it + '\n')
            }
        }
        if (message.to.size() == 0) {
            log.info('no message targets')
            render 'no message targets found'
            return
        }

        // assume the rest is the actual message...
        message.message = sections[1]
        message.receivedOn = new Date()

        log.info('message stored')
        message.save(flush: true, failOnError: true)


        render '<Response>\nTo:' + message.to.getAt(0).alias + '\nFrom:' + message.sender.name  + '\nMessage:' + message.message + '\n</Response>'

    }

    static class ContactSearch {

        static searchers = [new SearchByAlias(), new SearchByPhoneNumber()]

        def static List<MessageTarget> search(String messageTargetData) {
            List potentialMatches = []
            for (searcher in searchers) {
                def searchResult = searcher.search(messageTargetData)
                if (searchResult != null) {
                    potentialMatches.add(searchResult);
                }
            }
            return potentialMatches;
        }
    }

    static class SearchByAlias {

        MessageTarget search(String messageTargetData) {
            return MessageTarget.findByAlias(messageTargetData)
        }
    }

    static class SearchByPhoneNumber {
        MessageTarget search(String messageTargetData) {
            return Contact.findByPhoneNumber(messageTargetData)
        }
    }
}