package com.norian.megaphone.domain

import grails.rest.*

import com.norian.megaphone.domain.ApprovedSender
import com.norian.megaphone.domain.Shout

import java.util.regex.Matcher
import java.util.regex.Pattern


class ShoutController extends RestfulController {

    static responseFormats = ['json', 'xml']
    static Pattern toTargetPattern = Pattern.compile('^(TO:)?+(.*)$?', Pattern.CASE_INSENSITIVE)

    ShoutController() {
        super(Shout)
    }

    def index() {
        log.error("GET shouts")
        render Shout.list()
    }

    def save(Shout shout) {

        def errors = []

        log.info('new message received ' + params.toString() + '\t' + params.Body)

        Shout message = new Shout()
        message.messageSid = params['MessageSid']

        if (params['From'] == null) {
            log.info('no From specified')
            render 'Missing From'
        }

        def approvedSender = ApprovedSender.findByPhoneNumber(params['From'])
        if (approvedSender == null) {
            log.info('sender not approved: ' + params['From'])
            render 'not an approved sender'
            return;
        }
        message.sender = approvedSender

        String msg = params.Body
        if (msg == null || msg.isAllWhitespace()) {
            log.info('empty message body - shout ignored')
            render 'Missing Body'
            return;
        }

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