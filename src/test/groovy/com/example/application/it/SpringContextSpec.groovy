package com.example.application.it

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@SpringBootTest
class SpringContextSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    def "spring context loads"() {
        expect:
        applicationContext != null
        applicationContext.beanDefinitionCount > 0
    }
}