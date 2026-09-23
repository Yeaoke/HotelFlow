package com.example.app.models.main;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Rooms")
public class Room {
    
    @Id
    @UuidGenerator(style =  UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "home_type", nullable = false)
    private String homeType;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "has_tv", nullable = false)
    private Boolean hasTV;

    @Column(name = "has_internet", nullable = false)
    private Boolean hasInternet;

    @Column(name = "has_kitchen", nullable = false)
    private Boolean hasKitchen;

    @Column(name = "has_air_con", nullable = false)
    private Boolean hasAirCon;

    @Column(name = "price")
    private Long price;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false, referencedColumnName = "id")
    private User owner;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;
}
