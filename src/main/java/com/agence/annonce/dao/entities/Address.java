package com.agence.annonce.dao.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long address_id; 
    @Column(name = "governorate")
    private String governorate;
    @Column(name = "city")
    private String city;
    @Column(name = "street")
    private String street;
    @OneToOne(mappedBy="address")
    private Annonce annonce;
    public Address(String city, String governorate, String street) {
        this.city = city;
        this.governorate = governorate;
        this.street = street;
    }
 
}