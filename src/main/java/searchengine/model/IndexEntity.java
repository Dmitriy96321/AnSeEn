package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "`indexes`")
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private PageEntity pageId;

    @ManyToOne
    @JoinColumn(name = "lemma_id")
    private LemmaEntity lemmaId;

    @Column(name = "ranks")
    private Float rank;
}