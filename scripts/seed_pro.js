const { initializeApp } = require("firebase/app");
const { getFirestore, collection, getDocs, deleteDoc, doc, setDoc, addDoc } = require("firebase/firestore");

const firebaseConfig = {
  projectId: "app-reporte-4a5f9",
  apiKey: "AIzaSyD74Ms0v2DMluPFgekNiRSfBQp1CDONrU4"
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

async function wipeAndSeed() {
  console.log("Starting DB wipe...");
  const collections = ["classrooms", "users", "students", "courses", "attendance", "grades", "colegios", "forums", "posts", "complaints"];
  
  for (const coll of collections) {
     const snap = await getDocs(collection(db, coll));
     for (const d of snap.docs) {
        await deleteDoc(d.ref);
     }
     console.log(`Cleared ${coll} (${snap.size} docs)`);
  }
  
  console.log("Seeding colegios...");
  const colegiosData = [
    { name: "Colegio San Agustín", levels: ["Inicial", "Primaria", "Secundaria"] },
    { name: "Colegio Champagnat", levels: ["Inicial", "Primaria", "Secundaria"] },
    { name: "Colegio La Merced", levels: ["Primaria", "Secundaria"] }
  ];
  
  for (const c of colegiosData) {
      await setDoc(doc(db, "colegios", c.name), c);
  }

  console.log("Seeding classrooms...");
  const classroomsData = [
    { name: "4 Años - Inicial", level: "Inicial", grade: "4 Años", school_id: "Colegio San Agustín" },
    { name: "1er Grado A - Primaria", level: "Primaria", grade: "1er Grado", school_id: "Colegio San Agustín" },
    { name: "3er Año B - Secundaria", level: "Secundaria", grade: "3er Año", school_id: "Colegio San Agustín" },
    
    { name: "3 Años - Inicial", level: "Inicial", grade: "3 Años", school_id: "Colegio Champagnat" },
    { name: "2do Grado A - Primaria", level: "Primaria", grade: "2do Grado", school_id: "Colegio Champagnat" },
    { name: "5to Año A - Secundaria", level: "Secundaria", grade: "5to Año", school_id: "Colegio Champagnat" },
    
    { name: "6to Grado A - Primaria", level: "Primaria", grade: "6to Grado", school_id: "Colegio La Merced" },
    { name: "1er Año A - Secundaria", level: "Secundaria", grade: "1er Año", school_id: "Colegio La Merced" }
  ];
  
  const classRefs = {};
  for (const data of classroomsData) {
      const docRef = await addDoc(collection(db, "classrooms"), data);
      classRefs[data.name] = docRef.id;
  }
  
  console.log("Seeding users...");
  const users = [
      // Superadmin
      { email: "Angelrojaspacherres@gmail.com", password: "Password123!", rol: "superadmin", phone: "987654321", school_id: "Global" },
      
      // Admins
      { email: "marta.castillo@sanagustin.edu.pe", password: "Password123!", rol: "admin", phone: "+51922333444", school_id: "Colegio San Agustín" },
      { email: "carlos.mendoza@champagnat.edu.pe", password: "Password123!", rol: "admin", phone: "+51955666777", school_id: "Colegio Champagnat" },
      { email: "luisa.fernandez@lamerced.edu.pe", password: "Password123!", rol: "admin", phone: "+51988999000", school_id: "Colegio La Merced" },
      
      // Docentes
      { email: "jorge.ramirez@sanagustin.edu.pe", password: "Password123!", rol: "docente", phone: "+51911222333", school_id: "Colegio San Agustín", classrooms: [classRefs["4 Años - Inicial"], classRefs["1er Grado A - Primaria"]] },
      { email: "sofia.gutierrez@champagnat.edu.pe", password: "Password123!", rol: "docente", phone: "+51944555666", school_id: "Colegio Champagnat", classrooms: [classRefs["2do Grado A - Primaria"], classRefs["5to Año A - Secundaria"]] },
      { email: "ana.villanueva@lamerced.edu.pe", password: "Password123!", rol: "docente", phone: "+51977888999", school_id: "Colegio La Merced", classrooms: [classRefs["6to Grado A - Primaria"], classRefs["1er Año A - Secundaria"]] },
      
      // Padres
      { email: "marco.polo@gmail.com", password: "Password123!", rol: "usuario", phone: "+51900111222", school_id: "Colegio San Agustín" },
      { email: "elena.cortez@hotmail.com", password: "Password123!", rol: "usuario", phone: "+51933444555", school_id: "Colegio Champagnat" },
      { email: "david.martinez@outlook.com", password: "Password123!", rol: "usuario", phone: "+51966777888", school_id: "Colegio La Merced" }
  ];

  for (const u of users) {
      await setDoc(doc(db, "users", u.email), u);
  }

  console.log("Seeding students...");
  const students = [
      { names: "Leonardo", lastnames: "Polo", dni: "70111222", classroom_id: classRefs["4 Años - Inicial"], classroom_name: "4 Años - Inicial", school_id: "Colegio San Agustín", padre_id: "marco.polo@gmail.com", parent_email: "marco.polo@gmail.com", padre_name: "Marco Polo" },
      { names: "Daniela", lastnames: "Polo", dni: "70111223", classroom_id: classRefs["1er Grado A - Primaria"], classroom_name: "1er Grado A - Primaria", school_id: "Colegio San Agustín", padre_id: "marco.polo@gmail.com", parent_email: "marco.polo@gmail.com", padre_name: "Marco Polo" },
      
      { names: "Mateo", lastnames: "Cortez", dni: "70444555", classroom_id: classRefs["5to Año A - Secundaria"], classroom_name: "5to Año A - Secundaria", school_id: "Colegio Champagnat", padre_id: "elena.cortez@hotmail.com", parent_email: "elena.cortez@hotmail.com", padre_name: "Elena Cortez" },
      
      { names: "Renato", lastnames: "Martinez", dni: "70888999", classroom_id: classRefs["6to Grado A - Primaria"], classroom_name: "6to Grado A - Primaria", school_id: "Colegio La Merced", padre_id: "david.martinez@outlook.com", parent_email: "david.martinez@outlook.com", padre_name: "David Martinez" }
  ];

  for (const s of students) {
      const docRef = await addDoc(collection(db, "students"), s);
      s.id = docRef.id;
  }

  console.log("Seeding courses...");
  const courses = [
      { name: "Matemáticas", classroom_id: classRefs["1er Grado A - Primaria"], schedule: "Lunes 08:00 - 10:00", teacher_name: "Jorge Ramirez", teacher_id: "jorge.ramirez@sanagustin.edu.pe" },
      { name: "Comunicación", classroom_id: classRefs["1er Grado A - Primaria"], schedule: "Lunes 10:30 - 12:30", teacher_name: "Jorge Ramirez", teacher_id: "jorge.ramirez@sanagustin.edu.pe" },
      { name: "Ciencias", classroom_id: classRefs["1er Grado A - Primaria"], schedule: "Martes 08:00 - 10:00", teacher_name: "Jorge Ramirez", teacher_id: "jorge.ramirez@sanagustin.edu.pe" },
      
      { name: "Psicomotricidad", classroom_id: classRefs["4 Años - Inicial"], schedule: "Miércoles 09:00 - 11:00", teacher_name: "Jorge Ramirez", teacher_id: "jorge.ramirez@sanagustin.edu.pe" },
      
      { name: "Álgebra", classroom_id: classRefs["5to Año A - Secundaria"], schedule: "Lunes 08:00 - 10:00", teacher_name: "Sofía Gutierrez", teacher_id: "sofia.gutierrez@champagnat.edu.pe" },
      { name: "Física", classroom_id: classRefs["5to Año A - Secundaria"], schedule: "Martes 10:30 - 12:30", teacher_name: "Sofía Gutierrez", teacher_id: "sofia.gutierrez@champagnat.edu.pe" },
      
      { name: "Historia", classroom_id: classRefs["6to Grado A - Primaria"], schedule: "Lunes 09:00 - 11:00", teacher_name: "Ana Villanueva", teacher_id: "ana.villanueva@lamerced.edu.pe" },
      { name: "Geografía", classroom_id: classRefs["6to Grado A - Primaria"], schedule: "Martes 09:00 - 11:00", teacher_name: "Ana Villanueva", teacher_id: "ana.villanueva@lamerced.edu.pe" }
  ];

  for (const c of courses) {
      await addDoc(collection(db, "courses"), c);
  }

  console.log("Seeding attendance...");
  const attendance = [
      { student_id: students[0].id, classroom_id: classRefs["4 Años - Inicial"], date: "2026-07-01", status: "Presente", observations: "Llegó a tiempo." },
      { student_id: students[0].id, classroom_id: classRefs["4 Años - Inicial"], date: "2026-07-02", status: "Falta Justificada", observations: "Cita médica." },
      { student_id: students[0].id, classroom_id: classRefs["4 Años - Inicial"], date: "2026-07-03", status: "Presente", observations: "" },
      
      { student_id: students[1].id, classroom_id: classRefs["1er Grado A - Primaria"], date: "2026-07-01", status: "Presente", observations: "" },
      { student_id: students[1].id, classroom_id: classRefs["1er Grado A - Primaria"], date: "2026-07-02", status: "Tardanza", observations: "Llegó 15 min tarde." },
      
      { student_id: students[2].id, classroom_id: classRefs["5to Año A - Secundaria"], date: "2026-07-01", status: "Presente", observations: "" },
      { student_id: students[3].id, classroom_id: classRefs["6to Grado A - Primaria"], date: "2026-07-01", status: "Presente", observations: "" }
  ];

  for (const a of attendance) {
      await addDoc(collection(db, "attendance"), a);
  }

  console.log("Seeding grades...");
  const grades = [
      { student_id: students[0].id, student_name: "Leonardo Polo", classroom_id: classRefs["4 Años - Inicial"], subject: "Psicomotricidad", type: "Bimestre 1", value: "A", teacher_id: "jorge.ramirez@sanagustin.edu.pe", date: "2026-05-15", comments: "Excelente desarrollo motor." },
      
      { student_id: students[1].id, student_name: "Daniela Polo", classroom_id: classRefs["1er Grado A - Primaria"], subject: "Matemáticas", type: "Bimestre 1", value: "18", teacher_id: "jorge.ramirez@sanagustin.edu.pe", date: "2026-05-15", comments: "Muy buen razonamiento." },
      { student_id: students[1].id, student_name: "Daniela Polo", classroom_id: classRefs["1er Grado A - Primaria"], subject: "Comunicación", type: "Bimestre 1", value: "16", teacher_id: "jorge.ramirez@sanagustin.edu.pe", date: "2026-05-16", comments: "Mejorar caligrafía." },
      
      { student_id: students[2].id, student_name: "Mateo Cortez", classroom_id: classRefs["5to Año A - Secundaria"], subject: "Álgebra", type: "Bimestre 1", value: "15", teacher_id: "sofia.gutierrez@champagnat.edu.pe", date: "2026-05-15", comments: "Buen dominio de ecuaciones." },
      { student_id: students[2].id, student_name: "Mateo Cortez", classroom_id: classRefs["5to Año A - Secundaria"], subject: "Física", type: "Bimestre 1", value: "14", teacher_id: "sofia.gutierrez@champagnat.edu.pe", date: "2026-05-18", comments: "Falta entregar algunas tareas." },
      
      { student_id: students[3].id, student_name: "Renato Martinez", classroom_id: classRefs["6to Grado A - Primaria"], subject: "Historia", type: "Bimestre 1", value: "19", teacher_id: "ana.villanueva@lamerced.edu.pe", date: "2026-05-20", comments: "Participación brillante." }
  ];

  for (const g of grades) {
      await addDoc(collection(db, "grades"), g);
  }

  console.log("Seeding chats...");
  const chats = [
      { senderEmail: "marco.polo@gmail.com", receiverEmail: "jorge.ramirez@sanagustin.edu.pe", text: "Buenas tardes profesor, le escribo para justificar la falta de Leonardo.", timestamp: Date.now() - 86400000 },
      { senderEmail: "jorge.ramirez@sanagustin.edu.pe", receiverEmail: "marco.polo@gmail.com", text: "Buenas tardes señor Marco, no hay problema, ya registré la justificación en el sistema.", timestamp: Date.now() - 82000000 },
      
      { senderEmail: "elena.cortez@hotmail.com", receiverEmail: "sofia.gutierrez@champagnat.edu.pe", text: "Hola profesora, quisiera saber qué tal va Mateo en Álgebra.", timestamp: Date.now() - 3600000 },
      { senderEmail: "sofia.gutierrez@champagnat.edu.pe", receiverEmail: "elena.cortez@hotmail.com", text: "Hola Elena, Mateo va muy bien, aunque necesita repasar un poco factorización.", timestamp: Date.now() - 1800000 }
  ];

  for (const msg of chats) {
      await addDoc(collection(db, "chats"), msg);
  }
  
  console.log("Done!");
  process.exit(0);
}

wipeAndSeed().catch(e => {
    console.error(e);
    process.exit(1);
});
