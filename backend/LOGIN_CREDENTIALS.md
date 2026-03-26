# 🔐 Login Credentials for Campus Helper App

**Backend URL:** https://campushelper-be.onrender.com/api

---

## 📱 Available User Accounts

### Admin Accounts

#### 1. Sahil Shah (Main Admin) ⭐ UPDATED
- **Email:** `sahil8219@gmail.com`
- **Password:** `Sahil8219`
- **Role:** Admin
- **Access:** Full admin dashboard, manage subjects, materials, tests

#### 2. Campus Admin
- **Email:** `admin@campushelper.com`
- **Password:** `Admin123`
- **Role:** Admin
- **Access:** Full admin dashboard access

#### 3. Amit (Admin)
- **Email:** `amit123@gmail.com`
- **Password:** `Amit123`
- **Role:** Admin
- **Access:** Full admin dashboard access

---

### Student Accounts

#### 1. John Doe
- **Email:** `john@student.com`
- **Password:** `Password123`
- **Role:** Student

#### 2. Jane Smith
- **Email:** `jane@student.com`
- **Password:** `Password123`
- **Role:** Student

#### 3. Sahil Shah
- **Email:** `sahil123@gmail.com`
- **Password:** `Password123`
- **Role:** Student

#### 4. Vishal Dhiman
- **Email:** `vishaldhimanvish@gmail.com`
- **Password:** `Password123`
- **Role:** Student

#### 5. Suraj
- **Email:** `suraj6202k@gmail.com`
- **Password:** `Password123`
- **Role:** Student

---

## 📝 Notes

### Password Reset
If you need to reset passwords for all users again, run:
```bash
node fix-user-passwords.js
```

### Creating New Users
New users can register through the app with any email and password. They will automatically be assigned the **student** role.

### Admin Access
Only pre-existing admin accounts can access the admin dashboard. Student registration is restricted to students only.

---

## 🔧 Testing Login

### Test from Terminal
```bash
curl -X POST https://campushelper-be.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"amit123@gmail.com","password":"Amit123"}'
```

### Expected Response
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "68f9fc68b5d69534c99ee7a4",
    "name": "Amit",
    "email": "amit123@gmail.com",
    "role": "admin",
    "profileImage": null
  }
}
```

---

## ⚠️ Security Reminder

**Important:** These are test/demo credentials. For production:
1. Change all passwords to strong, unique passwords
2. Remove test accounts
3. Use secure password reset functionality
4. Enable two-factor authentication (if implemented)

---

**Last Updated:** November 13, 2025  
**Status:** All credentials tested and working ✅
