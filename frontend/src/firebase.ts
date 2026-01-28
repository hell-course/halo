// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";
// import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries
// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
    apiKey: "AIzaSyCld3_EMo_IdjKaif0FvsYEeLuVAtyJvew",
    authDomain: "halo-8f292.firebaseapp.com",
    projectId: "halo-8f292",
    storageBucket: "halo-8f292.firebasestorage.app",
    messagingSenderId: "459829431558",
    appId: "1:459829431558:web:7853750c4e45cce0ba6287",
    measurementId: "G-PWJD7VHBMH"
};
// Initialize Firebase
const app = initializeApp(firebaseConfig);
// const analytics = getAnalytics(app);
// Initialize Firebase Authentication and get a reference to the service
export const auth = getAuth(app);
// Initialize Google Auth Provider
export const googleProvider = new GoogleAuthProvider();
export default app;
