package com.example.application.ldap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.io.IOException;

@Data
@NoArgsConstructor
@Entry(
        base = "ou=users",
        objectClasses = {"inetOrgPerson"}
)
public final class LdapUserInfo {
    // this is the dn
    // "cn=Alex Smith+sn=Smith+uid=alexsmith+userPassword=1234,ou=users"
    @JsonSerialize(using = CustomNameJsonSerializer.class)
    @Id
    private Name id;
    @Attribute(name = "uid")
    private String uid;
    @Attribute(name = "cn")
    private String cn;
    @Attribute(name = "sn")
    private String sn;
    @Attribute(name = "givenName")
    private String givenName;
    @Attribute(name = "mail")
    private String email;
    @Attribute(name = "title")
    private String title;
    @Attribute(name = "departmentNumber")
    private String department;

    private static class CustomNameJsonSerializer extends StdSerializer<Name> {
        protected CustomNameJsonSerializer(Class<Name> t) {
            super(t);
        }

        protected CustomNameJsonSerializer() {
            this(null);
        }

        @Override
        public void serialize(Name value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }
}
