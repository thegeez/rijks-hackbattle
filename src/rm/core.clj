(ns rm.core
  (:require [clojure.xml :as xml]
            [clojure.java.io :as io]
            [clojure.string :as str]))
;; http://www.rijksmuseum.nl/api/oai/..API_KEY../?verb=listrecords&metadataPrefix=oai_dc




(defrecord Record []
  Object
  (toString [this]
            "RECORD_ITEM"))

(defmethod print-method Record [o w]
  (print-simple
   (str "#<Record "
        (str/join ", " 
               (for [[k v] o]
                 (condp = k
                     :content (str k " CONTENT")
                     (str k " " v))))
        ">") w))

(defn -main [& args]
  (with-open [file (io/input-stream (io/resource "rm_first_records_small.xml")
                                    :encoding "UTF-8"
                                    )]
    (->> (xml/parse file)
         :content
         (some (fn [{tag :tag :as node}]
                (when (= tag :ListRecords)
                  (:content node))))
         (map map->Record)))
  #_(let [fname (io/as-file (io/resource "rm_first_records_one.xml"))
        x (xml/parse fname)]
    x

    #_(->> x
        :content
        (some (fn [{tag :tag :as node}]
                (when (= tag :ListRecords)
                  (first (:content node)))))
        )))

(def nachtwacht
  "http://www.rijksmuseum.nl/api/oai/...API-KEY.../?verb=GetRecord&identifier=oai:rijksmuseum.nl/collection:COLLECT.5216&metadataPrefix=oai_dc")

;; &aria/maxwidth_288 &200x200

(def works-ids
  {"burgemeester" "SK-A-3830"
   "nachtwacht" "SK-C-5" ;; zie meesterwerken
   "staalmeesters" "SK-C-6"
   "schutters" "SK-C-2"
   "overlieden" "SK-C-3" ;; met persoonsnamen!
   "melkmeisje" "SK-A-2344" ;; vermeer (sj?)
   "liefdesbrief" "SK-A-1595"
   "vangogh" "SK-A-3262" ;;zelfportret oor, verfmethode
   "sluis" "SK-A-2704" 
   "wijkbijduurstede" "SK-C-211" ;;bootje
   "herengracht" "SK-A-5003"
   "bloemen" "SK-A-3907"
   "ijsvermaak" "SK-A-3286"
   "ijsvermaak2" "SK-A-1320"
   "ijsvermaak3" "SK-A-802"
   })


(defn print-getter [[name id]]
  (println "wget -O " (pr-str (str id ".xml")) " " (pr-str (str "http://www.rijksmuseum.nl/api/oai/...API-KEY../?verb=GetRecord&identifier=oai:rijksmuseum.nl/collection:" id "&metadataPrefix=oai_dc")))


  (println "wget -O " (pr-str (str id "_original.jpeg")) (pr-str (str "http://www.rijksmuseum.nl/media/assets/" id)))
  (println "wget -O " (pr-str (str id "_tiny.jpeg")) (pr-str (str "http://www.rijksmuseum.nl/media/assets/" id "?100x100")))
  (println "wget -O " (pr-str (str id "_small.jpeg")) (pr-str (str "http://www.rijksmuseum.nl/media/assets/" id "?200x200")))
  (println "wget -O " (pr-str (str id "_medium.jpeg")) (pr-str (str "http://www.rijksmuseum.nl/media/assets/" id "&aria/maxwidth_288"))))

(def works
  [{:id "SK-A-1595"
   :slug "liefdesbrief"
   :date "ca. 1669 - ca. 1670"
   :description "Voorstelling bekend als 'De liefdesbrief'. Gezicht door een openstaande deur naar een interieur waarin een dienstmaagd een brief overhandigt aan een jonge vrouw met een luit op schoot. Tegen de achtermuur twee schilderijen met een landschap en een zeegezicht. Op de voorgrond twee sloffen, een bezem en een stoel."
    :height 44
    :width 38.5
   :creator "schilder: Vermeer, Johannes"
   :title "'De liefdesbrief'"
   }
  {:id "SK-A-3830"
   :slug "burgemeester"
   :date "1757 -  1757"
   :description "Portret van Theodorus Bisdom van Vliet (1698-1777), burgemeester van Haastrecht en hoogheemraad van de Krimpenerwaard, met zijn gezin op een terras in de tuin van zijn huis te Haastrecht voor een beeld met Neptunus en Mercurius. Afgebeeld zijn verder: zijn vrouw Maria van Harthals (1703-63) en zijn kinderen linksboven: Cornelis (1737-73), Maria Theodora (1739-1828) en Adriana Elisabeth (1742-76), linksonder: Agatha (1743-76) Johanna Margaretha (1735-64) en Johan de Wijs (1740-62), met fluit, en rechts Elisabeth (1727-64) met waaier en gearmd met Marcellus (1729-1806), rechtsachter te paard Adriaan Jacob (1732-90) en Evert (1733-86)."
   :height 185
   :width 150
   :creator "schilder: Stolker, Jan"
   :title "Theodorus Bisdom van Vliet (1698-1777). Burgemeester van Haastrecht en hoogheemraad van de Krimpenerwaard, met zijn gezin in de tuin van zijn huis te Haastrecht"
   }
  {:id "SK-C-5"
   :slug "nachtwacht"
   :date "1642 -  1642"
   :description "Het korporaalschap van kapitein Frans Banninck Cocq en luitenant Willem van Ruytenburch, bekend als de 'Nachtwacht'. Schutters van de kloveniersdoelen uit een poort naar buiten tredend. Op een schild aangebracht naast de poort staan de namen van de afgebeelde personen: Frans Banninck Cocq, heer van purmerlant en Ilpendam, Capiteijn Willem van Ruijtenburch van Vlaerdingen, heer van Vlaerdingen, Lu[ij]tenant, Jan Visscher Cornelisen Vaendrich, Rombout Kemp Sergeant, Reijnier Engelen Sergeant, Barent Harmansen, Jan Adriaensen Keyser, Elbert Willemsen, Jan Clasen Leydeckers, Jan Ockersen, Jan Pietersen bronchorst, Harman Iacobsen wormskerck, Jacob Dircksen de Roy, Jan vander heede, Walich Schellingwou, Jan brugman, Claes van Cruysbergen, Paulus Schoonhoven. De schutters zijn gewapend met lansen, musketten en hellebaarden. Rechts de tamboer met een grote trommel. Tussen de soldaten links staat een meisje met een dode kip om haar middel, rechts een blaffende hond. Linksboven de vaandrig met de uitgestoken vaandel."
   :height 379.5
   :width 453.5
   :creator "schilder: Rembrandt Harmensz. van Rijn"
   :title "Officieren en andere schutters van wijk II in Amsterdam onder leiding van kapitein Frans Banninck Cocq en luitenant Willem van Ruytenburch, bekend als de 'Nachtwacht'"
   }
  {:id "SK-C-6"
   :slug "staalmeesters"
   :date "1662 -  1662"
   :description "De Staalmeesters: het college van staalmeesters (waardijns) van het Amsterdamse lakenbereidersgilde, bijeen rond een tafel waarop een Perzisch kleed ligt, op tafel het opengeslagen stalenboek. Voorgesteld zijn (van links naar rechts): Jacob van Loon (1595-1674), Volckert Jansz (1605/10-81), Willem van Doeyenburg (ca. 1616-87), de knecht Frans Hendricksz Bel (1629-1701), Aernout van der Mye (ca. 1625-81) en Jochem de Neve (1629-81). Rechts boven de haard een schilderij met een brandend baken."
   :height 191.5
   :width 279
   :creator "schilder: Rembrandt Harmensz. van Rijn"
   :title "De Staalmeesters: het college van staalmeesters (waardijns) van het Amsterdamse lakenbereidersgilde"
   }
  {:id "SK-C-2"
   :slug "schutters"
   :date "1648 -  1648"
   :description "De schuttersmaaltijd in de Voetboogdoelen of St. Jorisdoelen te Amsterdam ter viering van het sluiten van de vrede van Munster, 18 juni 1648. Voorgesteld zijn: kapitein Cornelis Jansz Witsen (met de zilveren drinkhoorn), luitenant Johan Oetgens van Waveren (die zijn hand schudt), de vaandrig Jacob Banningh (zittend naast de grote trommel), Dirck Claesz Thoveling en Thomas Hartog (sergeanten), Pieter van Hoorn, Willem Pietersz van der Voort, Adriaen Dirck Sparwer, Hendrick Calaber, Govert van der Mij, Johannes Calaber, Benedictus Schaeck, Jan Maes, Jacob van Diemen, Jan van Ommeren, Isaac Ooyens, Gerrit Pietersz van Anstenraadt, Herman Teunisz de Kluyter, Andries van Anstenraadt, Christoffel Poock, Hendrick Dommer Wz, Paulus Hennekijn, Lambregt van den Bos en Willem (de trommelslager). Op de grote trommel hangt een papier met een gedicht van Jan Vos. Door de openstaande ramen is de gevel van de brouwerij 'het Lam' aan de Singel zichtbaar. Rechts brengt een vrouwen een kalkoenpastei binnen. Op tafel staan tinnen borden, roemers en andere glazen. Links staat op de vloer een grote metalen koeler met een wijnvat."
   :height 232
   :width 547
   :creator "schilder: Helst, Bartholomeus van der"
   :title "De schuttersmaaltijd in de Voetboog- of St. Jorisdoelen te Amsterdam ter viering van het sluiten van de vrede van Munster, 18 juni 1648"
   }
  {:id "SK-C-3"
   :slug "overlieden"
   :date "1653 -  1657"
   :description "De vier overlieden van de Handboogdoelen of St Sebastiaandoelen te Amsterdam, 1653. De overlieden zijn van links naar rechts: Frans Banning Cocq, Jan van de Poll, Albert Dircksz Pater en Jan Willemsz Blaeu. De overlieden zitten rond een tafel voor een kast waarin pronkbekers zijn uitgestald. Zij houden ook in hun handen een pronkbokaal, de koningsstaf versierd met een vogel en de schuttersketen van de gilde met een medaillon in de vorm van een vogel. De doelvrouw brengt de ceremoniele zilveren drinkhoorn van de gilde binnen. Links zit een hond, rechts op de achtergrond twee schutters."
   :height 183
   :width 268
   :creator "schilder: Helst, Bartholomeus van der"
   :title "De vier overlieden van de Handboog- (St Sebastiaan) doelen te Amsterdam, 1653"
   }
  {:id "SK-A-2344" 
   :slug "melkmeisje"
   :date "ca. 1660 - ca. 1660"
   :description "Een dienstmaagd staat achter een tafel en schenkt melk uit een kruik in een aardewerken kom. Op de tafel staat een mand met brood en een stenen kruik, links een venster met een rieten mand en een koperen pot. Onderaan de muur rechts een rijtje tegels en een stoofje."
   :height 45.5
   :width 41
   :creator "schilder: Vermeer, Johannes"
   :title "Het melkmeisje"
   }
  {:id "SK-A-3262"
   :slug "vangogh"
   :date "1887"
   :description "Zelfportret van Vincent van Gogh. Borstbeeld met bruine jas en grijze hoed."
   :height 42
   :width 34
   :creator "schilder: van Gogh, Vincent"
   :title "Zelfportret van Gogh"
   }
  {:id "SK-A-2704"
   :slug "sluis"
   :date "1871"
   :description "Voorstelling van de Nieuwe Haarlemse Sluis bij het Singel te Amsterdam, genaamd 'Souvenir d'Amsterdam'. Een boot vaart door de sluis, aan de balie kijkt een man naar beneden. Geschilderd in Parijs naar een stereofoto."
   :height 46.5
   :width 35
   :creator "schilder: Maris, Matthijs"
   :title "De Nieuwe Haarlemse Sluis bij het Singel, genaamd 'Souvenir d'Amsterdam'"
   }
  {:id "SK-C-211"
   :slug "wijkbijduurstede"
   :date "ca. 1668 - ca. 1670"
   :description "De molen bij Wijk bij Duurstede. Links de rivier de Lek met een bootje, rechts de molen nabij de oever. In de verte de torens van kasteel Duurstede, rechts de toren van de Sint-Janskerk. Langs de oever lopen enkele vrouwen"
   :height 83
   :width 101
   :creator "schilder: Ruisdael, Jacob Isaacksz. van"
   :title "De molen bij Wijk bij Duurstede"
   }
  {:id "SK-A-5003"
   :slug "herengracht"
   :date "1671 -  1672"
   :description "Gezicht op de Amsterdamse Herengracht in aanbouw, gezien vanaf de brug van de Vijzelstraat. Bocht met in classicistische stijl opgetrokken grachtenpanden op de Herengracht met de ingang van de Nieuwe Spiegelstraat aan de linkerkant. Tussen de nog onvoltooide gevelrij valt zonlicht en reflecteert op het water van de gracht. Bouwmaterialen liggen verspreid op de kade. Schuttingen schermen de onbebouwde kavels af van de openbare weg. Het laatste pand aan de zuidzijde van de gracht staat zelfs nog in de steigers."
   :height 42.5
   :width 57.9
   :creator "schilder: Berckheyde, Gerrit Adriaensz."
   :title "Gezicht op de Herengracht in Amsterdam, vanaf de Vijzelstraat"
   }
  {:id "SK-A-3907"
   :slug "bloemen"
   :date "1762 -  1762"
   :description "Stilleven met bloemen. Op een tafel staat een glazen vaas met een boeket van tulpen, rozen, pioenen, anjers, irissen,  papavers en seringen."
   :height 76
   :width 64
   :creator "schilder: Mijn, Cornelia van der"
   :title "Stilleven met bloemen"
   }
  {:id "SK-A-3286"
   :slug "ijsvermaak"
   :date "1630 - 1679"
   :description "IJsvermaak buiten de wallen van een stad. Vooraan enkele mannen met kolfstokken. In het midden een arrenslede en schaatsers."
   :height 40
   :width 53.5
   :creator "schilder: Avercamp, Barend"
   :title "IJsvermaak"
   }
  {:id "SK-A-1320"
   :slug "ijsvermaak2"
   :date "ca. 1610 - ca. 1610"
   :description "IJsvermaak bij een dorp. Dorpsgezicht in de winter met vele figuren die op het ijs schaatsen, spelen en kolven. Links zijn twee personen door het ijs gezakt. In de verte een ophaalbrug en een molen."
   :height 35.7
   :width 70.4
   :creator "schilder: Avercamp, Hendrick"
   :title "Schaatsenrijden in een dorp"
   }
  {:id "SK-A-802"
   :slug "ijsvermaak3"
   :date "ca. 1615 - ca. 1620"
   :description "IJsvermaak op een stadsgracht, wellicht bij de Sint Janspoort te Haarlem. Een elegante dame kriijgt schaatsen ondergebonden, een groep mannen is aan het kolven, rechts een lange groep schaatsers. Links tegen de muur hurkt een poepende man."
   :height 47
   :width 86.8
   :creator "kopie naar schilder: Avercamp, Hendrick"
   :title "IJsvermaak op een stadsgracht"
   }]
  )
