const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write schools or students", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("schools").doc("main").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("students").get());
});

test("Authenticated user can create and read their own school config", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("schools").doc("main").set({
      userId: ALICE_UID,
      name: "ثانوية زمار النموذجية",
      address: "الموصل - زمار",
      schoolType: "GOVERNMENT",
      emblemTitle: "العلم نور والاجتهادرفعة",
      academicYear: "2026 - 2027",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("schools").doc("main").get()
  );
});

test("Cross-user isolation: Bob cannot read or modify Alice's students", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("students").doc("st_1").set({
      userId: ALICE_UID,
      fullName: "يوسف محمد العبيدي",
      gradeClass: "الصف الأول المتوسط",
      section: "أ",
      parentName: "محمد العبيدي",
      parentPhone: "07512362567",
      accessCode: "ST-1001",
      attendanceStatus: "PRESENT",
      absenceDaysMonth: 0,
      isHonorRoll: true,
      honorReason: "الأول على الصف بتفوق",
      totalTuition: 0,
      paidTuition: 0,
      nextInstallmentDate: "غير مطلوب",
      createdAt: now,
      updatedAt: now,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("students").doc("st_1").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("students").where("userId", "==", ALICE_UID).get()
  );
});

test("Shadow update / Ghost field rejected by strict hasOnly()", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("schools").doc("main").set({
      userId: ALICE_UID,
      name: "ثانوية زمار النموذجية",
      address: "الموصل - زمار",
      schoolType: "GOVERNMENT",
      emblemTitle: "العلم نور",
      academicYear: "2026 - 2027",
      isVerifiedGhostField: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Client query alignment: Alice can list her own students with userId filter", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).collection("students").doc("st_1").set({
      userId: ALICE_UID,
      fullName: "يوسف محمد العبيدي",
      gradeClass: "الصف الأول المتوسط",
      section: "أ",
      parentName: "محمد العبيدي",
      parentPhone: "07512362567",
      accessCode: "ST-1001",
      attendanceStatus: "PRESENT",
      absenceDaysMonth: 0,
      isHonorRoll: true,
      honorReason: "الأول على الصف",
      totalTuition: 0,
      paidTuition: 0,
      nextInstallmentDate: "غير مطلوب",
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("students").where("userId", "==", ALICE_UID).get()
  );
});
