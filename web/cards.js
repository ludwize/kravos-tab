// Картице: осам песама из сета, свака учи један појам из теорије. Редослед је редослед учења.
const CARDS = [
  {
    id: "tnt", title: "T.N.T.", artist: "AC/DC", year: "1975",
    concept: "Корен и пулс",
    facts: [["Тоналитет", "E (између дура и мола)"], ["Темпо", "око 126 BPM"]],
    chords: [["Риф и рефрен", "A – G – E (iv – ♭III – i)"]],
    theory: "Све почиње од корена: нота по којој се акорд зове. Бас свира равне осминке на корену и мења ноту тачно кад и гитара. G изнад E је мала терца (♭III), зато песма звучи „прљаво“ рокерски, иако нема мол акорда у класичном смислу.",
    listen: "Бас и бас-бубањ ударају заједно. Паузе у рефрену су део ритма, тишина мора да буде тачна колико и нота.",
    exercises: [
      "Уз снимак свирај само целе ноте на корену: E, G, A. Циљ је да чујеш где се акорд мења.",
      "Исто у равним осминкама на 70% темпа. Без убрзавања, свака осминка једнака.",
      "Цео рефрен уз снимак, бројећи у себи „1 и 2 и“. Промена акорда пада тачно на 1."
    ],
    sources: [["Hooktheory: T.N.T.", "https://www.hooktheory.com/theorytab/view/ac-dc/tnt"], ["Jon MacLennan: TNT chords", "https://www.jonmaclennan.com/blog/tnt-chords"]]
  },
  {
    id: "paranoid", title: "Paranoid", artist: "Black Sabbath", year: "1970",
    concept: "Мол пентатоника",
    facts: [["Тоналитет", "E мол"]],
    chords: [["Цела песма", "E5, D5, G5, C5 (пауер акорди)"]],
    theory: "E мол пентатоника је пет нота: E G A H D. Нема полустепена између њих, зато ниједна не „смета“ преко рок акорда. То је прва „кутија“ на врату коју треба да знаш напамет.",
    listen: "Geezer и Iommi свирају риф заједно, бас октаву ниже. Geezer каже да баш то даје Sabbath рифовима тежину. Чује се као један дебео инструмент.",
    exercises: [
      "Нађи E мол пентатонику у првој позицији (празне жице и 2–3. праг) и свирај је горе-доле.",
      "Свирај риф уз снимак и препознај које ноте пентатонике користи.",
      "Импровизуј само из пентатонике преко соло дела. Ниједна нота неће звучати погрешно."
    ],
    sources: [["Wikipedia: Paranoid", "https://en.wikipedia.org/wiki/Paranoid_(Black_Sabbath_song)"], ["Jon MacLennan: Paranoid chords", "https://www.jonmaclennan.com/blog/paranoid-chords"], ["Guitar World: Geezer Butler", "https://www.guitarworld.com/features/geezer-butlers-10-best-basslines-with-black-sabbath"]]
  },
  {
    id: "aceofspades", title: "Ace of Spades", artist: "Motörhead", year: "1980",
    concept: "Квинта и двозвук",
    facts: [["Штим", "пола тона ниже (E♭)"], ["Центар", "главни риф на A"]],
    chords: [["Главни риф", "A"], ["Строфе", "мења се испод главног рифа, нађи по уху"]],
    theory: "Квинта (5) је нота седам полустепена изнад корена. На басу је увек на истом месту: два прага више на суседној тањој жици. Корен и квинта заједно су пауер акорд на басу. Lemmy је бас свирао као гитару: двозвуци, празна жица која звони и мелодија на нижој жици.",
    listen: "Бас звучи као ритам гитара, дисторзија и пуни акорди. Не тражи појединачне ноте, тражи облик.",
    exercises: [
      "Свирај корен и квинту заједно на A, D, G и C. Облик прстију је увек исти.",
      "Уз снимак прво само корен у осминкама, па пређи на двозвук корен и квинта.",
      "Нађи по уху где се у строфи мења акорд и на који. Без таба, ово је вежба за уво."
    ],
    sources: [["TalkingBass: Ace of Spades", "https://www.talkingbass.net/heaviest-bass-riff-of-all-time-ace-of-spades/"], ["Guitar World: Lemmy", "https://guitarworld.com/features/lemmys-5-greatest-basslines-with-motorhead"]]
  },
  {
    id: "wastedyears", title: "Wasted Years", artist: "Iron Maiden", year: "1986",
    concept: "i – ♭VII – ♭VI",
    facts: [["Тоналитет", "E мол"], ["Темпо", "око 154 BPM"]],
    chords: [["Строфа", "Em – D – C"], ["Рефрен", "G – C – Am – D – C – Am – Em – D"]],
    theory: "Em – D – C је најчешћи метал покрет у молу: тоника, па два корака доле. Рефрен креће од G (паралелни дур), зато звучи ведрије иако су ноте исте. Гитаре мењају акорд осминку раније (антиципација), а бас прати.",
    listen: "Последња осминка пред тактну црту често није корен, то је „гурање“ акорда унапред. Пред крај рефрена бас се хроматски пење ка G.",
    exercises: [
      "Само корени: E, D, C у строфи, G, C, A, D у рефрену.",
      "Прилаз одоздо: последња осминка пред промену је полустепен испод новог корена (D# пред E, H пред C).",
      "У рефрену држи E преко C и Am. То је терца C-дура и квинта Am, заједнички тон."
    ],
    sources: [["Анализа твог GP фајла (Kravos Tab)", ""]]
  },
  {
    id: "aintalkin", title: "Ain't Talkin' 'bout Love", artist: "Van Halen", year: "1978",
    concept: "Исти образац, други тоналитет",
    facts: [["Штим", "пола тона ниже: хваташ A мол, звучи G♯ мол"]],
    chords: [["Риф", "Am – F – G (i – ♭VI – ♭VII)"]],
    theory: "Ово су исти акорди као у Wasted Years (i, ♭VI, ♭VII), само други редослед и други тоналитет. Кад научиш облик на врату, носиш га у сваки тоналитет: прсти раде исто, само почињеш са другог прага.",
    listen: "Гитара разлаже акорде, а бас држи корен испод. Ти си под.",
    exercises: [
      "Свирај корене A, F, G уз снимак. Проштимуј пола тона ниже или укључи Tune Down на ME-90B.",
      "Исти облик пребаци на E (Em – C – D), па на D (Dm – B – C). Прсти исто, звук други.",
      "Уради исто са Wasted Years покретом (i – ♭VII – ♭VI) у A молу: Am – G – F."
    ],
    sources: [["Hooktheory: Ain't Talkin' 'bout Love", "https://www.hooktheory.com/theorytab/view/van-halen/aint-talkin-bout-love"], ["MusicRadar: weekend riff", "https://musicradar.com/tuition/guitars/weekend-riff-van-halen-aint-talkin-bout-love-607083"]]
  },
  {
    id: "wholelotta", title: "Whole Lotta Love", artist: "Led Zeppelin", year: "1969",
    concept: "Блуз: мала терца и мала септима",
    facts: [["Центар", "E"], ["Темпо", "око 92 BPM"]],
    chords: [["Риф", "стоји на E"], ["Рефрен", "E – D (i – ♭VII)"]],
    theory: "Блуз живи на две ноте: мала терца (G над E) и мала септима (D над E). Кад E мол пентатоници из Paranoid-а додаш B♭, добијаш блуз скалу: E G A B♭ H D. B♭ је „блу нота“, пролази се преко ње, не стоји се на њој.",
    listen: "John Paul Jones свира риф заједно са гитаром, октаву ниже. Корени песме су у блузу Willie Dixon-а („You Need Love“).",
    exercises: [
      "Свирај E мол пентатонику, па додај B♭ између A и H. То је блуз скала.",
      "Свирај само E и D (R и ♭7) у ритму рифа. Чућеш блуз и без осталих нота.",
      "Импровизуј из блуз скале преко рифа. На B♭ само прођи."
    ],
    sources: [["Wikipedia: Whole Lotta Love", "https://en.wikipedia.org/wiki/Whole_Lotta_Love"], ["Hooktheory: Whole Lotta Love", "https://www.hooktheory.com/theorytab/view/led-zeppelin/whole-lotta-love"]]
  },
  {
    id: "aceshigh", title: "Aces High", artist: "Iron Maiden", year: "1984",
    concept: "Галоп и педал тон",
    facts: [["Темпо", "око 140 BPM"]],
    chords: [["Увод", "F♯m – D – E"], ["Строфа", "Em – D – Am – G"], ["Рефрен", "Em – C – D, па G – E♭ – F"], ["Соло", "Am – F – G"]],
    theory: "Галоп је осмина и две шеснаестине („та-тата“), Harris-ов потпис. Педал тон значи да бас држи исту ноту док се акорди мењају изнад ње, као сидро. Пред крај рефрена G – E♭ – F је ♭VI – ♭VII, исти покрет као у Van Halen-у, али овде диже песму.",
    listen: "Галоп мора да буде раван као машина. Кад се акорди мењају, бас мења само ноту, никад ритам.",
    exercises: [
      "Галоп на празној E жици уз метроном: 80, па 100, па 120 BPM.",
      "Галоп преко корена строфе: E, D, A, G.",
      "Рефрен у петљи уз снимак (A–B у Kravos Player-у), прво на 75% темпа."
    ],
    sources: [["Chordwiki: Aces High", "https://ja.chordwiki.org/wiki/Aces+High"], ["GuitarTabsExplorer: Aces High", "https://www.guitartabsexplorer.com/iron-maiden-Tabs/aces-high-crd.php"], ["Music I Want: Aces High", "https://www.musiciwant.com/song/aces-high-iron-maiden"]]
  },
  {
    id: "bell", title: "For Whom the Bell Tolls", artist: "Metallica", year: "1984",
    concept: "Мелодијски бас и хроматика",
    facts: [["Центар", "E (Hooktheory: E дорски)"], ["Темпо", "око 116–118 BPM"]],
    chords: [["Увод", "i – ii (E5 – F♯5)"]],
    theory: "Хроматика је кретање по полустепену, праг по праг. Дорски је мол са великом секстом: у E молу уместо C свираш C♯, и мол одједном звучи мање тамно. Cliff Burton је увод написао пре Metallica-е и свира га као соло: дисторзија, wah и високи регистар.",
    listen: "У уводу бас води, гитаре прате. Cliff је снимио три бас траке: дисторзирани соло, чист бас и хармонике.",
    exercises: [
      "Хроматска скала од E до A на једној жици, полако, сваки прст један праг.",
      "Свирај E мол лествицу, па замени C са C♯. Чуј како дорски мења боју.",
      "Уз снимак прво корени главног рифа. Увод тек кад прве две вежбе легну."
    ],
    sources: [["Wikipedia: For Whom the Bell Tolls", "https://en.wikipedia.org/wiki/For_Whom_the_Bell_Tolls_(Metallica_song)"], ["Hooktheory: For Whom the Bell Tolls", "https://hooktheory.com/theorytab/view/metallica/for-whom-the-bell-tolls"], ["Bass Musician Magazine", "https://bassmusicianmagazine.com/2015/10/bass-lines-by-jaime-vazquez-part-xv-metallicas-for-whom-the-bell-tolls/amp/"]]
  }
];
