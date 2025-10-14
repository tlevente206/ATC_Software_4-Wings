ATC Ops – kézi adatú repülőtéri forgalomkezelő

Az ATC Ops egy Java alapú rendszer (Spring Boot + JavaFX), amellyel a csapat 100%-ban kézi adatokból fel tud építeni és működtetni repterek forgalmi nézeteit. 
A felhasználók megtekinthetik az induló/érkező járatokat és az aktív gépek állapotát, az admin pedig a járatokat és gépadatokat kézzel kezeli, indulásokat halaszt, illetve magasságot állít. 
A backend az időzített feladatokkal a járatokat automatikusan a megadott ütemezés szerint indítja/érkezteti, és konfliktusriasztást ad, ha két gép veszélyesen közel kerül egymáshoz. 
Minden admin-módosítás és automatika azonnal megjelenik a kliensoldalon egy könnyű változás-feed (delta-polling) segítségével. Semmilyen külső API-t nem használunk.
