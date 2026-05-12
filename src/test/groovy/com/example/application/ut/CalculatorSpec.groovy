package com.example.application.ut

import spock.lang.Specification
import spock.lang.Unroll

class CalculatorSpec extends Specification {

    def "adds two numbers"() {
        expect:
        2 + 3 == 5
    }

    @Unroll
    def "max(#a, #b) should be #expected"() {
        expect:
        Math.max(a, b) == expected

        where:
        a | b || expected
        1 | 2 || 2
        5 | 3 || 5
        0 | 0 || 0
    }
}