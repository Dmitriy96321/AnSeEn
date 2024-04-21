package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "`index`")
public class IndexEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private SiteEntity pageId;

    @ManyToOne
    @JoinColumn(name = "lemma_id")
    private SiteEntity lemmaId;

    @Column(name = "rank")
    private Float rank;

}