import { useState } from 'react';
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
  useMediaQuery,
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { useForm } from 'react-hook-form';
import ProviderTable from './ProviderTable';
import './ProviderLogin.css';
import ProviderAvailability from './ProviderAvailability';

const mockUsers = [
  { email: 'provider@example.com', password: 'Password123', locked: false },
  { email: 'locked@example.com', password: 'Password123', locked: true },
];

const dummyProviders = [
  {
    firstName: 'John', lastName: 'Doe', email: 'john.doe@clinic.com', phone: '+12345678901', specialization: 'Cardiology', license: 'A12345', experience: 10, street: '123 Main St', city: 'Metropolis', state: 'NY', zip: '10001', status: 'active'
  },
  {
    firstName: 'Jane', lastName: 'Smith', email: 'jane.smith@clinic.com', phone: '+19876543210', specialization: 'Dermatology', license: 'B67890', experience: 7, street: '456 Elm St', city: 'Gotham', state: 'CA', zip: '90001', status: 'active'
  }
];

const defaultProviders: any[] = [...dummyProviders];

const ProviderLogin = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [authError, setAuthError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loggedIn, setLoggedIn] = useState(false);
  const [openDialog, setOpenDialog] = useState(false);
  const [providers, setProviders] = useState<any[]>(defaultProviders);
  const [, setNewProvider] = useState<any | null>(null);
  const isMobile = useMediaQuery('(max-width:600px)');

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
      const user = mockUsers.find(u => u.email === data.credential);
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

  // Handle provider registration from the separate component
  const handleProviderRegistration = (providerData: any) => {
    const newProviderWithStatus = { ...providerData, status: 'active' };
    
    setProviders(prev => [...prev, newProviderWithStatus]);
    setNewProvider(newProviderWithStatus);
    setOpenDialog(false);
    
    // Show success message
    alert('Provider registered successfully! They can now log in with their credentials.');
  };

  const handleEditProvider = (provider: any) => {
    alert(`Edit provider: ${provider.firstName} ${provider.lastName}`);
  };

  const handleDeleteProvider = (index: number) => {
    if (window.confirm('Are you sure you want to delete this provider?')) {
      setProviders(prev => prev.filter((_, i) => i !== index));
    }
  };

  const handleViewProvider = (provider: any) => {
    alert(`View provider details:\nName: ${provider.firstName} ${provider.lastName}\nEmail: ${provider.email}\nPhone: ${provider.phone}\nSpecialization: ${provider.specialization}\nLicense: ${provider.license}\nExperience: ${provider.experience} years\nAddress: ${provider.street}, ${provider.city}, ${provider.state} ${provider.zip}\nStatus: ${provider.status}`);
  };

  return (
    <Box className={`provider-login-root${isMobile ? ' mobile' : ''}`}
      sx={{
        minHeight: '100vh',
        minWidth: '100vw',
        height: '100vh',
        width: '100vw',
        p: 0,
        overflow: 'hidden',
        bgcolor: '#f5f7fa',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}>
      {!loggedIn ? (
        <Box className="provider-login-container"
          sx={{
            width: isMobile ? '95vw' : 400,
            maxWidth: 400,
            minHeight: isMobile ? 'auto' : 480,
            boxShadow: 3,
            borderRadius: 3,
            bgcolor: '#fff',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            m: 0,
            p: 3,
          }}>
          <Typography variant="h5" className="provider-login-title" gutterBottom>
            Provider Portal Login
          </Typography>
          <form onSubmit={handleSubmit(onLogin)} autoComplete="off" className="provider-login-form" style={{ width: '100%' }}>
            <TextField
              fullWidth
              id="credential"
              label="Email or Phone"
              variant="outlined"
              margin="normal"
              {...register('credential', {
                required: 'Email or phone is required',
                validate: value => {
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
            <FormControl fullWidth variant="outlined" margin="normal">
              <InputLabel htmlFor="password" error={!!errors.password}>
                Password
              </InputLabel>
              <OutlinedInput
                id="password"
                type={showPassword ? 'text' : 'password'}
                {...register('password', {
                  required: 'Password is required',
                  minLength: { value: 8, message: 'Password must be at least 8 characters' },
                })}
                error={!!errors.password}
                disabled={loading}
                endAdornment={
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
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
                <Typography variant="caption" color="error" className="provider-login-error">
                  {errors.password.message}
                </Typography>
              )}
            </FormControl>
            <Box className="provider-login-options">
              <FormControlLabel
                control={
                  <Checkbox
                    id="remember"
                    color="primary"
                    {...register('remember')}
                    disabled={loading}
                  />
                }
                label="Remember Me"
              />
              <Button
                className="provider-login-forgot"
                size="small"
                tabIndex={loading ? -1 : 0}
                disabled={loading}
                onClick={() => alert('Forgot password flow (not implemented)')}
              >
                Forgot Password?
              </Button>
            </Box>
            {authError && (
              <Alert severity="error" className="provider-login-alert">
                {authError}
              </Alert>
            )}
            {success && (
              <Alert severity="success" className="provider-login-alert">
                Login successful! Redirecting...
              </Alert>
            )}
            <Button
              color="primary"
              variant="contained"
              fullWidth
              type="submit"
              className="provider-login-btn"
              disabled={loading || !isValid || !isDirty}
              startIcon={loading ? <CircularProgress size={20} /> : null}
              sx={{ mt: 2 }}
            >
              {loading ? 'Signing In...' : 'Sign In'}
            </Button>
            
            <Box sx={{ mt: 2, textAlign: 'center' }}>
              <Typography variant="body2" color="textSecondary" sx={{ mb: 1 }}>
                Don't have an account?
              </Typography>
              <Button
                variant="outlined"
                color="primary"
                fullWidth
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
                Sign Up as Provider
              </Button>
            </Box>
          </form>
        </Box>
      ) : (
        <Box sx={{ 
          width: '100vw', 
          height: '100vh', 
          bgcolor: '#f5f7fa', 
          p: isMobile ? 1 : 4, 
          display: 'flex', 
          flexDirection: 'column',
          overflow: 'hidden'
        }}>
          <Box sx={{ width: '100%', display: 'flex', justifyContent: 'flex-end', alignItems: 'center', mb: 2 }}>
            <Button 
              variant="contained" 
              color="primary" 
              onClick={() => setOpenDialog(true)} 
              sx={{ 
                fontWeight: 600, 
                fontSize: '1rem', 
                px: 3, 
                py: 1.2, 
                borderRadius: 2, 
                boxShadow: 2,
                marginTop:2,
                background: 'linear-gradient(45deg, #1976d2 30%, #42a5f5 90%)',
                '&:hover': {
                  background: 'linear-gradient(45deg, #1565c0 30%, #1976d2 90%)',
                  boxShadow: 4,
                  transform: 'translateY(-1px)',
                },
                transition: 'all 0.3s ease'
              }}
            >
              + Availability
            </Button>
          </Box>
          <Box sx={{ 
            width: '100%', 
            height: '100%',
            overflow: 'auto',
            flex: 1
          }}>
            <ProviderTable 
              providers={providers} 
              onEdit={handleEditProvider}
              onDelete={handleDeleteProvider}
              onView={handleViewProvider}
            />
          </Box>
        </Box>
      )}

      {/* Use the separate ProviderRegistrationForm component */}
      {/* <ProviderRegistrationForm
        open={openDialog}
        onClose={() => setOpenDialog(false)}
        onRegister={handleProviderRegistration}
        existingProviders={providers}
      /> */}
      <ProviderAvailability 
        open={openDialog} 
        onClose={() => setOpenDialog(false)} 
      />

    </Box>
  );
};

export default ProviderLogin;
