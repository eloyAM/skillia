package com.example.application.ldap;

import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LdapUserRepository extends LdapRepository<LdapUserInfo> {
}
