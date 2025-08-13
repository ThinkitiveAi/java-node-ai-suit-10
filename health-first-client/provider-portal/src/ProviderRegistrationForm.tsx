import React, { useState } from 'react';
import { 
  Dialog, 
  DialogContent, 
  DialogTitle, 
  Button, 
  TextField, 
  Box, 
  MenuItem,
  FormControl,
  InputLabel,
  OutlinedInput,
  InputAdornment,
  IconButton,
  Typography
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { useForm } from 'react-hook-form';

type Provider = {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  specialization: string;
  license: string;
  experience: string;
  street: string;
  city: string;
  state: string;
  zip: string;
  status: 'active' | 'inactive' | 'on leave';
  password: string;
  confirmPassword: string;
};

type ProviderRegistrationFormProps = {
  open: boolean;
  onClose: () => void;
  onRegister: (provider: Omit<Provider, 'password' | 'confirmPassword'>) => void;
  existingProviders: Omit<Provider, 'password' | 'confirmPassword'>[];
};

const ProviderRegistrationForm: React.FC<ProviderRegistrationFormProps> = ({ 
  open, 
  onClose, 
  onRegister, 
  existingProviders 
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isDirty, isValid },
    reset,
    setError,
    watch,
  } = useForm<Provider>({
    mode: 'onChange',
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      specialization: '',
      license: '',
      experience: '',
      street: '',
      city: '',
      state: '',
      zip: '',
      status: 'active',
      password: '',
      confirmPassword: '',
    },
  });

  const watchPassword = watch('password');

  const handleRegister = (data: Provider) => {
    if (existingProviders.some((p) => p.email === data.email)) {
      setError('email', { type: 'manual', message: 'Email already exists' });
      return;
    }
    if (existingProviders.some((p) => p.phone === data.phone)) {
      setError('phone', { type: 'manual', message: 'Phone already exists' });
      return;
    }
    if (existingProviders.some((p) => p.license === data.license)) {
      setError('license', { type: 'manual', message: 'License already exists' });
      return;
    }

    // Remove password fields before passing to parent (in real app, password would be hashed and stored securely)
    const { password, confirmPassword, ...providerData } = data;
    onRegister(providerData);
    reset();
  };

  const handleClose = () => {
    onClose();
    reset();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth PaperProps={{ sx: { borderRadius: 4, p: 2 } }}>
      <DialogTitle sx={{ textAlign: 'center', fontWeight: 700, fontSize: '1.5rem' }}>
        Provider Registration
      </DialogTitle>
      <DialogContent>
        <form onSubmit={handleSubmit(handleRegister)} style={{ marginTop: 8 }}>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, justifyContent: 'center' }}>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="First Name" 
                fullWidth 
                {...register('firstName', { 
                  required: 'First name required', 
                  minLength: { value: 2, message: 'Min 2 chars' }, 
                  maxLength: { value: 50, message: 'Max 50 chars' } 
                })} 
                error={!!errors.firstName} 
                helperText={errors.firstName?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="Last Name" 
                fullWidth 
                {...register('lastName', { 
                  required: 'Last name required', 
                  minLength: { value: 2, message: 'Min 2 chars' }, 
                  maxLength: { value: 50, message: 'Max 50 chars' } 
                })} 
                error={!!errors.lastName} 
                helperText={errors.lastName?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="Email Address" 
                fullWidth 
                type="email" 
                {...register('email', { 
                  required: 'Email required', 
                  pattern: { value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: 'Invalid email' } 
                })} 
                error={!!errors.email} 
                helperText={errors.email?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="Phone Number" 
                fullWidth 
                type="tel" 
                {...register('phone', { 
                  required: 'Phone required', 
                  pattern: { value: /^\+?\d{10,15}$/, message: 'Invalid phone' } 
                })} 
                error={!!errors.phone} 
                helperText={errors.phone?.message} 
              />
            </Box>
            
            {/* Password Field */}
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <FormControl fullWidth variant="outlined">
                <InputLabel htmlFor="reg-password" error={!!errors.password}>
                  Password
                </InputLabel>
                <OutlinedInput
                  id="reg-password"
                  type={showPassword ? 'text' : 'password'}
                  {...register('password', {
                    required: 'Password is required',
                    minLength: { value: 8, message: 'Password must be at least 8 characters' },
                    pattern: {
                      value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
                      message: 'Password must contain uppercase, lowercase, number and special character'
                    }
                  })}
                  error={!!errors.password}
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
                  <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.5 }}>
                    {errors.password.message}
                  </Typography>
                )}
              </FormControl>
            </Box>

            {/* Confirm Password Field */}
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <FormControl fullWidth variant="outlined">
                <InputLabel htmlFor="confirm-password" error={!!errors.confirmPassword}>
                  Confirm Password
                </InputLabel>
                <OutlinedInput
                  id="confirm-password"
                  type={showConfirmPassword ? 'text' : 'password'}
                  {...register('confirmPassword', {
                    required: 'Please confirm your password',
                    validate: value => value === watchPassword || 'Passwords do not match'
                  })}
                  error={!!errors.confirmPassword}
                  endAdornment={
                    <InputAdornment position="end">
                      <IconButton
                        aria-label="toggle confirm password visibility"
                        onClick={() => setShowConfirmPassword((show) => !show)}
                        edge="end"
                        tabIndex={-1}
                      >
                        {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                      </IconButton>
                    </InputAdornment>
                  }
                  label="Confirm Password"
                />
                {errors.confirmPassword && (
                  <Typography variant="caption" color="error" sx={{ mt: 0.5, ml: 1.5 }}>
                    {errors.confirmPassword.message}
                  </Typography>
                )}
              </FormControl>
            </Box>

            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="Specialization" 
                fullWidth 
                {...register('specialization', { 
                  required: 'Specialization required', 
                  minLength: { value: 3, message: 'Min 3 chars' }, 
                  maxLength: { value: 100, message: 'Max 100 chars' } 
                })} 
                error={!!errors.specialization} 
                helperText={errors.specialization?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="Medical License Number" 
                fullWidth 
                {...register('license', { 
                  required: 'License required', 
                  pattern: { value: /^[a-zA-Z0-9]+$/, message: 'Alphanumeric only' } 
                })} 
                error={!!errors.license} 
                helperText={errors.license?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="Years of Experience" 
                fullWidth 
                type="number" 
                inputProps={{ min: 0, max: 50 }} 
                {...register('experience', { 
                  required: 'Experience required', 
                  min: { value: 0, message: 'Min 0' }, 
                  max: { value: 50, message: 'Max 50' } 
                })} 
                error={!!errors.experience} 
                helperText={errors.experience?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="Street Address" 
                fullWidth 
                {...register('street', { 
                  required: 'Street required', 
                  maxLength: { value: 200, message: 'Max 200 chars' } 
                })} 
                error={!!errors.street} 
                helperText={errors.street?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="City" 
                fullWidth 
                {...register('city', { 
                  required: 'City required', 
                  maxLength: { value: 100, message: 'Max 100 chars' } 
                })} 
                error={!!errors.city} 
                helperText={errors.city?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="State/Province" 
                fullWidth 
                {...register('state', { 
                  required: 'State required', 
                  maxLength: { value: 50, message: 'Max 50 chars' } 
                })} 
                error={!!errors.state} 
                helperText={errors.state?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="ZIP/Postal Code" 
                fullWidth 
                {...register('zip', { 
                  required: 'ZIP required', 
                  pattern: { value: /^[a-zA-Z0-9\-\s]{3,12}$/, message: 'Invalid ZIP' } 
                })} 
                error={!!errors.zip} 
                helperText={errors.zip?.message} 
              />
            </Box>
            <Box sx={{ minWidth: 250, maxWidth: 400, flex: '1 1 300px' }}>
              <TextField 
                label="Status" 
                fullWidth 
                select
                {...register('status', { required: 'Status required' })} 
                error={!!errors.status} 
                helperText={errors.status?.message}
              >
                <MenuItem value="active">Active</MenuItem>
                <MenuItem value="inactive">Inactive</MenuItem>
                <MenuItem value="on leave">On Leave</MenuItem>
              </TextField>
            </Box>
          </Box>
          <Box sx={{ width: '100%', display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
            <Button onClick={handleClose} color="secondary" variant="outlined">
              Cancel
            </Button>
            <Button type="submit" color="primary" variant="contained" disabled={!isValid || !isDirty}>
              Register Provider
            </Button>
          </Box>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ProviderRegistrationForm;