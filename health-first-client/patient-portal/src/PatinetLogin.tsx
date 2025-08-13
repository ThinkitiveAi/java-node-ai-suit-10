import  { useState } from 'react';
import {
  Box,
  Button,
  Checkbox,
  CircularProgress,
  FormControl,
  FormControlLabel,
  IconButton,
  InputAdornment,
  InputLabel,
  OutlinedInput,
  TextField,
  Typography,
  Alert,
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { useForm } from 'react-hook-form';
import PatientRegistrationForm from './PatientRegistrationForm';


const mockPatients = [
  { email: 'patient@example.com', password: 'Password123', locked: false },
  { email: 'locked@example.com', password: 'Password123', locked: true },
];

const dummyPatients = [
  {
    firstName: 'Alice',
    lastName: 'Johnson',
    email: 'alice.johnson@email.com',
    phone: '+12345678901',
    dateOfBirth: '1990-05-15',
    gender: 'female',
    street: '789 Oak St',
    city: 'Springfield',
    state: 'IL',
    zip: '62701',
    emergencyContact: {
      name: 'Bob Johnson',
      phone: '+19876543210',
      relationship: 'Spouse'
    },
    insurance: {
      provider: 'Blue Cross',
      policyNumber: 'BC123456789'
    },
    medicalHistory: ['Hypertension', 'Diabetes Type 2']
  },
  {
    firstName: 'Michael',
    lastName: 'Brown',
    email: 'michael.brown@email.com',
    phone: '+19876543211',
    dateOfBirth: '1985-12-03',
    gender: 'male',
    street: '456 Pine Ave',
    city: 'Chicago',
    state: 'IL',
    zip: '60601',
    emergencyContact: {
      name: 'Sarah Brown',
      phone: '+12345678902',
      relationship: 'Sister'
    },
    insurance: {
      provider: 'Aetna',
      policyNumber: 'AET987654321'
    },
    medicalHistory: ['Asthma']
  }
];

const defaultPatients: any[] = [...dummyPatients];

const PatientLogin = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [authError, setAuthError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loggedIn, setLoggedIn] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [patients, setPatients] = useState(defaultPatients);

  // Login form
  const {
    register,
    handleSubmit,
    formState: { errors, isDirty, isValid },
 
  } = useForm({
    mode: 'onChange',
    defaultValues: {
      credential: '',
      password: '',
      remember: false,
    },
  });

  const onLogin = (data: any) => {
    setAuthError('');
    setLoading(true);
    setSuccess(false);

    setTimeout(() => {
      if (data.credential === 'network@error.com') {
        setAuthError('Network/server error. Please try again.');
        setLoading(false);
        return;
      }

      const user = mockPatients.find(u => u.email === data.credential);

      if (!user) {
        setAuthError('Account not found.');
        setLoading(false);
        return;
      }

      if (user.locked) {
        setAuthError('Account is locked. Please contact support.');
        setLoading(false);
        return;
      }

      if (user.password !== data.password) {
        setAuthError('Incorrect password.');
        setLoading(false);
        return;
      }

      setSuccess(true);
      setLoading(false);
      setTimeout(() => {
        setSuccess(false);
        setLoggedIn(true);
      }, 1000);
    }, 1000);
  };

  const handleRegisterPatient = (patientData: any) => {
    const newPatient = { ...patientData };
    setPatients(prev => [...prev, newPatient]);
    setOpenDialog(false);
    alert('Patient registered successfully! You can now log in with your credentials.');
  };

  const handleEditPatient = (patient: any, index: number) => {
    alert(`Edit patient: ${patient.firstName} ${patient.lastName}`);
  };

  const handleDeletePatient = (index: number) => {
    if (window.confirm('Are you sure you want to delete this patient?')) {
      setPatients(prev => prev.filter((_, i) => i !== index));
    }
  };

  const handleViewPatient = (patient: any) => {
    const emergencyInfo = patient.emergencyContact ? 
      `\nEmergency Contact: ${patient.emergencyContact.name} (${patient.emergencyContact.relationship}) - ${patient.emergencyContact.phone}` : 
      '\nEmergency Contact: Not provided';
    
    const insuranceInfo = patient.insurance ? 
      `\nInsurance: ${patient.insurance.provider} - Policy: ${patient.insurance.policyNumber}` : 
      '\nInsurance: Not provided';
    
    const medicalInfo = patient.medicalHistory && patient.medicalHistory.length > 0 ? 
      `\nMedical History: ${patient.medicalHistory.join(', ')}` : 
      '\nMedical History: Not provided';

    alert(`Patient Details:\nName: ${patient.firstName} ${patient.lastName}\nEmail: ${patient.email}\nPhone: ${patient.phone}\nDate of Birth: ${patient.dateOfBirth}\nGender: ${patient.gender}\nAddress: ${patient.street}, ${patient.city}, ${patient.state} ${patient.zip}${emergencyInfo}${insuranceInfo}${medicalInfo}`);
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        // background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 2,
      }}
    >
      {!loggedIn ? (
        <Box
          sx={{
            width: '100%',
            maxWidth: 400,
            mx: 'auto',
            p: 4,
            bgcolor: 'background.paper',
            borderRadius: 4,
            boxShadow: 24,
          }}
        >
          <Typography
            variant="h4"
            component="h1"
            gutterBottom
            sx={{
              textAlign: 'center',
              fontWeight: 600,
              color: 'primary.main',
              mb: 3,
            }}
          >
            Patient Portal Login
          </Typography>

          <Box component="form" onSubmit={handleSubmit(onLogin)}>
            <TextField
              fullWidth
              margin="normal"
              label="Email or Phone"
              {...register('credential', {
                required: 'Email or phone number is required',
                validate: (value) => {
                  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                  const phoneRegex = /^\+?\d{10,15}$/;
                  if (!emailRegex.test(value) && !phoneRegex.test(value)) {
                    return 'Enter a valid email or phone number';
                  }
                  return true;
                },
              })}
              error={!!errors.credential}
              helperText={errors.credential?.message}
              disabled={loading}
              autoFocus
            />

            <FormControl fullWidth margin="normal" variant="outlined">
              <InputLabel htmlFor="password">Password</InputLabel>
              <OutlinedInput
                id="password"
                type={showPassword ? 'text' : 'password'}
                {...register('password', {
                  required: 'Password is required',
                  minLength: {
                    value: 6,
                    message: 'Password must be at least 6 characters',
                  },
                })}
                error={!!errors.password}
                disabled={loading}
                endAdornment={
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowPassword((show) => !show)}
                      edge="end"
                      tabIndex={-1}
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                }
                label="Password"
              />
              {errors.password && (
                <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 2 }}>
                  {errors.password.message}
                </Typography>
              )}
            </FormControl>

            <FormControlLabel
              control={<Checkbox {...register('remember')} />}
              label="Remember Me"
              sx={{ mt: 1 }}
            />

            <Button
              variant="text"
              size="small"
              onClick={() => alert('Forgot password flow (not implemented)')}
              sx={{ mt: 1, display: 'block' }}
            >
              Forgot Password?
            </Button>

            {authError && (
              <Alert severity="error" sx={{ mt: 2 }}>
                {authError}
              </Alert>
            )}

            {success && (
              <Alert severity="success" sx={{ mt: 2 }}>
                Login successful! Redirecting...
              </Alert>
            )}

            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={loading || !isDirty || !isValid}
              sx={{ mt: 2 }}
            >
              {loading && <CircularProgress size={20} sx={{ mr: 1 }} />}
              {loading ? 'Signing In...' : 'Sign In'}
            </Button>

            {/* Sign Up Button */}
            <Box sx={{ mt: 2, textAlign: 'center' }}>
              <Typography variant="body2" sx={{ mb: 1 }}>
                Don't have an account?
              </Typography>
              <Button
                variant="outlined"
                onClick={() => setOpenDialog(true)}
                disabled={loading}
                sx={{
                  borderColor: '#1976d2',
                  color: '#1976d2',
                  '&:hover': {
                    borderColor: '#1565c0',
                    backgroundColor: 'rgba(25, 118, 210, 0.04)',
                  },
                }}
              >
                Sign Up as Patient
              </Button>
            </Box>
          </Box>
        </Box>
      ) : (
        <Box sx={{ width: '100%', maxWidth: 1200, mx: 'auto' }}>
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              mb: 3,
              bgcolor: 'background.paper',
              p: 2,
              borderRadius: 2,
            }}
          >
            <Typography variant="h4" component="h1" sx={{ color: 'primary.main', fontWeight: 600 }}>
              Patient Management
            </Typography>
            <Button
              variant="contained"
              onClick={() => setOpenDialog(true)}
              sx={{
                fontWeight: 600,
                fontSize: '1rem',
                px: 3,
                py: 1.2,
                borderRadius: 2,
                boxShadow: 2,
                background: 'linear-gradient(45deg, #1976d2 30%, #42a5f5 90%)',
                '&:hover': {
                  background: 'linear-gradient(45deg, #1565c0 30%, #1976d2 90%)',
                  boxShadow: 4,
                  transform: 'translateY(-1px)',
                },
                transition: 'all 0.3s ease'
              }}
            >
              + Register New Patient
            </Button>
          </Box>

          {/* <PatientTable
            patients={patients}
            onEdit={handleEditPatient}
            onDelete={handleDeletePatient}
            onView={handleViewPatient}
          /> */}
        </Box>
      )}

      {/* Patient Registration Dialog */}
      <PatientRegistrationForm
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        onRegister={handleRegisterPatient}
        existingPatients={patients}
      />
    </Box>
  );
};

export default PatientLogin;
