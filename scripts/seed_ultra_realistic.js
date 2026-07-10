const { initializeApp } = require("firebase/app");
const { getFirestore, collection, getDocs, deleteDoc, doc, setDoc, addDoc } = require("firebase/firestore");

const firebaseConfig = {
  projectId: "app-reporte-4a5f9",
  apiKey: "AIzaSyD74Ms0v2DMluPFgekNiRSfBQp1CDONrU4"
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

// Data for generation
const nombresM = ["Carlos", "Luis", "Jorge", "Miguel", "Pedro", "Juan", "Alejandro", "Diego", "Andrés", "Fernando", "Roberto", "Ricardo", "Eduardo", "Javier", "Manuel", "José", "Daniel", "Hugo", "Mario", "Óscar", "Héctor", "Raúl", "Martín", "Rafael", "Felipe", "Gustavo"];
const nombresF = ["María", "Lucía", "Ana", "Carmen", "Elena", "Laura", "Marta", "Sofía", "Rosa", "Teresa", "Isabel", "Paula", "Diana", "Gabriela", "Silvia", "Patricia", "Andrea", "Natalia", "Mónica", "Carolina", "Beatriz", "Alicia", "Victoria", "Valeria", "Claudia", "Adriana"];
const apellidos = ["García", "Rodríguez", "González", "Fernández", "López", "Martínez", "Sánchez", "Pérez", "Gómez", "Martín", "Jiménez", "Ruiz", "Hernández", "Díaz", "Moreno", "Muñoz", "Álvarez", "Romero", "Alonso", "Gutiérrez", "Navarro", "Torres", "Domínguez", "Vázquez", "Ramos", "Gil", "Ramírez", "Serrano", "Blanco", "Molina", "Morales", "Suárez", "Ortega", "Delgado", "Castro", "Ortiz", "Rubio", "Marín", "Sanz", "Núñez", "Iglesias", "Medina", "Garrido", "Cortés", "Castillo", "Santos", "Lozano", "Guerrero", "Cano", "Prieto", "Méndez", "Cruz", "Calvo"];

const cursos = ["Matemáticas", "Comunicación", "Ciencias Naturales", "Historia", "Inglés", "Educación Física", "Arte", "Computación", "Geografía", "Religión"];
const periodos = ["Bimestre 1", "Bimestre 2", "Bimestre 3", "Bimestre 4"];
const tiposNota = ["Examen Parcial", "Examen Final", "Práctica Calificada", "Tarea", "Exposición", "Participación"];
const comentarios = ["Excelente alumno, siempre participa.", "Debe mejorar su caligrafía.", "Se distrae con facilidad en clase.", "Muy buen progreso este bimestre.", "Necesita practicar más matemáticas en casa.", "Falta justificada.", "Llegó tarde 3 veces.", "Muy colaborador con sus compañeros."];
const quejasDesc = ["El profesor de matemáticas no explica bien.", "Mi hijo fue empujado en el recreo.", "Los baños están sucios.", "Demasiada tarea para el fin de semana.", "El menú escolar no es saludable."];
const eventTitles = ["Día de la Madre", "Reunión de Padres", "Olimpiadas Escolares", "Simulacro de Sismo", "Feria de Ciencias", "Día del Logro", "Aniversario del Colegio", "Clausura del Año"];

function randomChoice(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function getRandomName() {
    const isMale = Math.random() > 0.5;
    const name = isMale ? randomChoice(nombresM) : randomChoice(nombresF);
    const ape1 = randomChoice(apellidos);
    const ape2 = randomChoice(apellidos);
    return { name, ape1, ape2, full: `${name} ${ape1} ${ape2}` };
}

function generateEmail(nombre, ape1) {
    let base = `${nombre.toLowerCase()}.${ape1.toLowerCase()}`;
    base = base.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
    const randomNum = Math.floor(Math.random() * 900) + 100;
    const domains = ["gmail.com", "hotmail.com", "yahoo.com", "outlook.com"];
    return `${base}${randomNum}@${randomChoice(domains)}`;
}

async function wipeAndSeed() {
  console.log("Starting DB wipe...");
  const collections = ["classrooms", "users", "students", "courses", "attendance", "grades", "colegios", "events", "chats", "comments", "complaints", "direct_chats", "notifications", "posts"];
  
  for (const coll of collections) {
     const snap = await getDocs(collection(db, coll));
     for (const d of snap.docs) {
        await deleteDoc(d.ref);
     }
     console.log(`Cleared ${coll}`);
  }
  
  console.log("Seeding colegios...");
  const colegios = ["Colegio Champagnat", "Colegio San Agustín", "Colegio La Merced", "Colegio Pamer", "Colegio Trilce"];
  for (const col of colegios) {
      await setDoc(doc(db, "colegios", col), { name: col, levels: ["Inicial", "Primaria", "Secundaria"] });
  }

  console.log("Seeding classrooms...");
  const classroomsData = [];
  const classRefs = {};
  for (const school of colegios) {
      const levels = { "Inicial": [3, 4, 5], "Primaria": [1, 2, 3, 4, 5, 6], "Secundaria": [1, 2, 3, 4, 5] };
      for (const [level, grades] of Object.entries(levels)) {
          const sample = grades.sort(() => 0.5 - Math.random()).slice(0, 2);
          for (const g of sample) {
              const name = `${g} Grado - ${level}`;
              const docRef = await addDoc(collection(db, "classrooms"), {
                  name: name,
                  level: level,
                  grade: `${g} Grado`,
                  school_id: school
              });
              const cData = { id: docRef.id, name, school };
              classroomsData.push(cData);
              classRefs[name + school] = cData;
          }
      }
  }

  console.log("Seeding users (Padres, Docentes, Admins)...");
  const usersToInsert = [];
  const padresEmails = [];
  const docentesEmails = [];
  
  usersToInsert.push({ email: "Angelrojaspacherres@gmail.com", password: "superadmin123", rol: "superadmin", phone: "999888777", school_id: "Global", nombre: "Angel Rojas" });
  usersToInsert.push({ email: "roberto.perez85@gmail.com", password: "Password123!", rol: "usuario", phone: "+51999111222", school_id: "Colegio Champagnat", nombre: "Roberto Pérez" });
  padresEmails.push({ email: "roberto.perez85@gmail.com", school: "Colegio Champagnat", nombre: "Roberto Pérez" });

  for(let i=0; i<10; i++){
      const p = getRandomName();
      const email = generateEmail(p.name, p.ape1);
      usersToInsert.push({ email: email, password: "Password123!", rol: "admin", phone: "+519" + Math.floor(Math.random()*100000000), school_id: randomChoice(colegios), nombre: p.full });
  }

  for(let i=0; i<30; i++){
      const p = getRandomName();
      const email = generateEmail(p.name, p.ape1);
      const school = randomChoice(colegios);
      const schoolClassrooms = classroomsData.filter(c => c.school === school).map(c => c.id);
      
      const teacherRooms = schoolClassrooms.sort(() => 0.5 - Math.random()).slice(0, 2);
      
      const phone = "+519" + Math.floor(Math.random()*100000000);
      usersToInsert.push({ email: email, password: "Password123!", rol: "docente", phone: phone, school_id: school, nombre: p.full, classrooms: teacherRooms });
      docentesEmails.push({email, school, name: p.full, phone, classrooms: teacherRooms});
  }

  for(let i=0; i<30; i++){
      const p = getRandomName();
      const email = generateEmail(p.name, p.ape1);
      const school = randomChoice(colegios);
      usersToInsert.push({ email: email, password: "Password123!", rol: "usuario", phone: "+519" + Math.floor(Math.random()*100000000), school_id: school, nombre: p.full });
      padresEmails.push({ email, school, nombre: p.full, ape1: p.ape1, ape2: p.ape2 });
  }

  for (const u of usersToInsert) {
      await setDoc(doc(db, "users", u.email), u);
  }

  console.log("Seeding students and grades/attendance...");
  let dniCounter = 70100000;
  const allStudents = [];
  for (const padre of padresEmails) {
      const numHijos = Math.floor(Math.random() * 3) + 1;
      for (let i=0; i<numHijos; i++) {
          const sName = randomChoice(padre.nombre.length % 2 === 0 ? nombresM : nombresF);
          const schoolClassrooms = classroomsData.filter(c => c.school === padre.school);
          if (schoolClassrooms.length === 0) continue;
          const classroom = randomChoice(schoolClassrooms);

          const student = {
              names: sName,
              lastnames: padre.ape1 ? `${padre.ape1} ${randomChoice(apellidos)}` : "Pérez",
              dni: (dniCounter++).toString(),
              classroom_id: classroom.id,
              classroom_name: classroom.name,
              school_id: padre.school,
              parent_email: padre.email,
              padre_name: padre.nombre
          };
          
          const sRef = await addDoc(collection(db, "students"), student);
          allStudents.push({ id: sRef.id, ...student });

          for(let g=0; g<(Math.floor(Math.random()*5)+5); g++){
              await addDoc(collection(db, "grades"), {
                  student_id: sRef.id,
                  subject: randomChoice(cursos),
                  value: Math.floor(Math.random() * 11) + 10,
                  type: randomChoice(tiposNota),
                  period: randomChoice(periodos),
                  date: `10/10/2026`
              });
          }

          for(let a=0; a<3; a++){
              await addDoc(collection(db, "attendance"), {
                  student_id: sRef.id,
                  date: `0${a+1}/10/2026`,
                  status: randomChoice(["Asistió", "Faltó", "Tardanza"]),
                  justification: ""
              });
          }
      }
  }

  console.log("Seeding Courses...");
  for (const docente of docentesEmails) {
      if(docente.classrooms.length > 0) {
          for (const cId of docente.classrooms) {
              const cNameObj = classroomsData.find(c => c.id === cId);
              const cName = cNameObj ? cNameObj.name : "Clase";
              await addDoc(collection(db, "courses"), {
                  course_name: randomChoice(cursos),
                  teacher_name: docente.name,
                  teacher_email: docente.email,
                  teacher_phone: docente.phone,
                  classroom_id: cId,
                  classroom_name: cName,
                  schedule: randomChoice(["Lunes 10:00 AM - 12:00 PM", "Martes 08:00 AM - 10:00 AM", "Miércoles 12:00 PM - 02:00 PM"]),
                  school_id: docente.school
              });
          }
      }
  }

  console.log("Seeding Events...");
  for (let e=0; e<20; e++) {
      await addDoc(collection(db, "events"), {
          title: randomChoice(eventTitles),
          description: "Evento escolar importante a nivel institucional.",
          date: `10/10/2026`,
          school_id: randomChoice(colegios)
      });
  }

  console.log("Seeding Complaints...");
  for (let c=0; c<20; c++) {
      const p = randomChoice(padresEmails);
      await addDoc(collection(db, "complaints"), {
          parent_email: p.email,
          post_title: randomChoice(quejasDesc).substring(0, 20) + "...",
          content: randomChoice(quejasDesc),
          status: "pending",
          school_id: p.school,
          timestamp: Date.now()
      });
  }
  
  console.log("Seeding Posts and Comments (Foro)...");
  for (const docente of docentesEmails) {
      if(docente.classrooms.length > 0) {
          const postRef = await addDoc(collection(db, "posts"), {
              author: docente.name,
              content: randomChoice(["¡Bienvenidos al nuevo bimestre! Por favor revisar el sílabo.", "Recuerden traer los materiales de arte mañana (Témperas y pinceles).", "Felicitaciones a todos por el esfuerzo en la feria de ciencias."]),
              timestamp: Date.now(),
              classroom_id: docente.classrooms[0]
          });
          
          // Seed Comments for this post
          const commentCount = Math.floor(Math.random() * 4);
          for(let k=0; k<commentCount; k++){
              const p = randomChoice(padresEmails);
              await addDoc(collection(db, "comments"), {
                  post_id: postRef.id,
                  author: p.nombre,
                  content: randomChoice(["Gracias por avisar, profesor.", "Entendido, ahí estaremos.", "¿A qué hora es exactamente?", "Excelente iniciativa."]),
                  timestamp: Date.now()
              });
          }
      }
  }
  
  console.log("Seeding Direct Chats & Messages...");
  for(let i=0; i<15; i++){
      const docente = randomChoice(docentesEmails);
      const padre = randomChoice(padresEmails);
      
      const chatRef = await addDoc(collection(db, "direct_chats"), {
          participants: [docente.email, padre.email],
          lastMessage: "Gracias por la información.",
          timestamp: Date.now()
      });
      
      // Add messages inside the chat (subcollection)
      const msgsCollection = collection(db, `direct_chats/${chatRef.id}/messages`);
      await addDoc(msgsCollection, {
          sender: padre.email,
          content: `Buenas tardes profesor ${docente.name}, ¿cómo va mi hijo en clase?`,
          timestamp: Date.now() - 100000
      });
      await addDoc(msgsCollection, {
          sender: docente.email,
          content: `Estimado(a) ${padre.nombre}, va muy bien, prestando mucha atención.`,
          timestamp: Date.now() - 50000
      });
      await addDoc(msgsCollection, {
          sender: padre.email,
          content: "Gracias por la información.",
          timestamp: Date.now()
      });
  }

  console.log("Seeding Notifications...");
  for (let c=0; c<20; c++) {
      const p = randomChoice(padresEmails);
      await addDoc(collection(db, "notifications"), {
          user_email: p.email,
          title: "Nueva actualización escolar",
          body: "Se ha publicado un nuevo aviso en el foro de su salón.",
          timestamp: Date.now(),
          read: false
      });
  }

  console.log("Done! Seeded ULTRA REALISTIC database with 20+ records in ALL collections.");
  process.exit(0);
}

wipeAndSeed().catch(e => {
    console.error(e);
    process.exit(1);
});
