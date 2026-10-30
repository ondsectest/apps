// People who shaped software testing, shown in a row at the bottom of the New Tab page.
//
// Photos: put the original photos in a folder, named by slug (e.g. james-bach.jpg), then run
//   python3 scripts/prepare_photos.py <folder>
// It crops and tones every photo the same way, saves them to new-tab/people/, and fills in
// `photo` below. Only add photos you have permission to share. Without a photo the card
// shows the person's initials.

const PEOPLE = [
  {
    slug: "james-bach",
    name: "James Bach",
    known: "Co-creator of Rapid Software Testing",
    url: "https://www.satisfice.com/",
    photo: "people/james-bach.jpg",
  },
  {
    slug: "michael-bolton",
    name: "Michael Bolton",
    known: "Co-creator of Rapid Software Testing",
    url: "https://developsense.com/",
    photo: "people/michael-bolton.jpg",
  },
  {
    slug: "robert-sabourin",
    name: "Robert Sabourin",
    known: "Author of “I Am a Bug!”",
    url: "mailto:RobSab@gmail.com",
    photo: "people/robert-sabourin.jpg",
  },
  {
    slug: "cem-kaner",
    name: "Cem Kaner",
    known: "Lead author of “Testing Computer Software”",
    url: "https://kaner.com/",
    photo: "people/cem-kaner.jpg",
  },
  {
    slug: "jerry-weinberg",
    name: "Jerry Weinberg",
    known: "Author of “The Psychology of Computer Programming”",
    url: "https://en.wikipedia.org/wiki/Gerald_Weinberg",
    photo: "people/jerry-weinberg.jpg",
  },
];
