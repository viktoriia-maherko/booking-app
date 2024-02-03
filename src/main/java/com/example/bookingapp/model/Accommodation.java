package com.example.bookingapp.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Data
@Table(name = "accommodations")
@SQLDelete(sql = "UPDATE accommodations SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar")
    private Type type;
    @OneToOne
    private Address address;
    @Column(nullable = false)
    private String size;
    @ElementCollection
    @CollectionTable(name = "accommodations_amenities",
            indexes = { @Index(columnList = "list_index") },
            joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(nullable = false)
    private List<String> amenities;
    @Column(nullable = false)
    private BigDecimal dailyRate;
    @Column(nullable = false)
    private Integer availability;
    @Column(nullable = false)
    private boolean isDeleted = false;

    public enum Type {
        HOUSE, APARTMENT, CONDO, VACATION_HOME
    }
}
