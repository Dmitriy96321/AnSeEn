package searchengine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lemma")
public class LemmaEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private SiteEntity siteId;


    @Column(name = "lemma")
    private String lemma;

    @Column(name = "frequency")
    private Integer frequency;

    @Override
    public String toString() {
        return "LemmaEntity{" +
                "lemma='" + lemma + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}