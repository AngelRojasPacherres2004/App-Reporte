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
  const collections = ["classrooms", "users", "students", "courses", "attendance", "grades", "forums"];
  
  for (const coll of collections) {
     const snap = await getDocs(collection(db, coll));
     for (const d of snap.docs) {
        await deleteDoc(d.ref);
     }
     console.log(`Cleared ${coll}`);
  }
  
  console.log("Seeding classrooms...");
  const classroomsData = [
    { name: "3 Años", level: "Inicial", grade: "3 Años", school_id: "Colegio San José" },
    { name: "4 Años", level: "Inicial", grade: "4 Años", school_id: "Colegio San José" },
    { name: "5 Años", level: "Inicial", grade: "5 Años", school_id: "Colegio San José" },
    
    { name: "1er Grado A - Primaria", level: "Primaria", grade: "1er Grado", school_id: "Colegio San José" },
    { name: "1er Grado B - Primaria", level: "Primaria", grade: "1er Grado", school_id: "Colegio San José" },
    { name: "2do Grado A - Primaria", level: "Primaria", grade: "2do Grado", school_id: "Colegio San José" },
    
    { name: "1er Año A - Secundaria", level: "Secundaria", grade: "1er Año", school_id: "Colegio San José" },
    { name: "1er Año B - Secundaria", level: "Secundaria", grade: "1er Año", school_id: "Colegio San José" }
  ];
  
  const classRefs = {};
  for (const data of classroomsData) {
      const docRef = await addDoc(collection(db, "classrooms"), data);
      classRefs[data.name] = docRef.id;
  }
  
  console.log("Seeding users...");
  const admin = {
      email: "admin@sanjose.com",
      password: "Password123!",
      rol: "admin",
      phone: "+51987654321",
      school_id: "Colegio San José"
  };
  await setDoc(doc(db, "users", admin.email), admin);
  
  const docente = {
      email: "docente1@reporte.com",
      password: "Password123!",
      rol: "docente",
      phone: "+51999888777",
      school_id: "Colegio San José",
      classrooms: [classRefs["1er Grado A - Primaria"], classRefs["1er Año A - Secundaria"]]
  };
  await setDoc(doc(db, "users", docente.email), docente);
  
  const padre = {
      email: "padre1@reporte.com",
      password: "Password123!",
      rol: "usuario",
      phone: "+51999111222",
      school_id: "Colegio San José"
  };
  await setDoc(doc(db, "users", padre.email), padre);

  console.log("Seeding students...");
  const student1 = {
      first_name: "Juanito",
      last_name: "Pérez",
      dni: "12345678",
      classroom_id: classRefs["1er Grado A - Primaria"],
      classroom_name: "1er Grado A - Primaria",
      school_id: "Colegio San José",
      padre_id: padre.email,
      padre_email: padre.email,
      padre_name: "Papá Pérez"
  };
  await addDoc(collection(db, "students"), student1);

  const student2 = {
      first_name: "Maria",
      last_name: "Gomez",
      dni: "87654321",
      classroom_id: classRefs["1er Año A - Secundaria"],
      classroom_name: "1er Año A - Secundaria",
      school_id: "Colegio San José",
      padre_id: padre.email,
      padre_email: padre.email,
      padre_name: "Mamá Gomez"
  };
  await addDoc(collection(db, "students"), student2);
  
  console.log("Done!");
  process.exit(0);
}

wipeAndSeed().catch(e => {
    console.error(e);
    process.exit(1);
});
