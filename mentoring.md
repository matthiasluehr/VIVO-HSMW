# Mentoring Model

```mermaid
classDiagram
    class Role
    
    class Person

    class Organization

    class Publication

    class wissArbeit
    wissArbeit : String Thema
    wissArbeit : String NameDesPromovenden
    wissArbeit : Date startDate
    wissArbeit : Date endDate
    wissArbeit : Boolean hsmwPromoStatus

    class Abschlussarbeit

    class Bachelor

    class Diplom

    class Master

    class AdvisorRole
    
    Role <|-- AdvisorRole

    class AdviseeRole

    Role <|-- AdviseeRole

    wissArbeit <|-- Abschlussarbeit
    Abschlussarbeit <|-- Bachelor
    Abschlussarbeit <|-- Diplom
    Abschlussarbeit <|-- Master

    wissArbeit .. AdvisorRole : vivo_contributingRole
    wissArbeit .. AdviseeRole : vivo_contributingRole
    wissArbeit .. Organization : vido-de_Kooperationspartner
    wissArbeit .. Publication : bibo_presents

    Person .. AdviseeRole : obo_bearerOf
    Person .. AdvisorRole : obo_bearerOf
    ```
