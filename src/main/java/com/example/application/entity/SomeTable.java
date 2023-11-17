package com.example.application.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "some_table")
class SomeTable {
    @Column(name = "name")
    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "a_required_val", nullable = false)
    private Integer aRequiredVal;

    public Integer getARequiredVal() {
        return aRequiredVal;
    }

    public void setARequiredVal(Integer aRequiredVal) {
        this.aRequiredVal = aRequiredVal;
    }

    @Column(name = "a_unique_val", unique = true)
    private String aUniqueVal;

    public String getAUniqueVal() {
        return aUniqueVal;
    }

    public void setAUniqueVal(String aUniqueVal) {
        this.aUniqueVal = aUniqueVal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}