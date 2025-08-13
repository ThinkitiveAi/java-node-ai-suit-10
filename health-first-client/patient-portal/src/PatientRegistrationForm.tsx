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
  Typography,
  Chip,
  FormHelperText,
  LinearProgress,
  Collapse,
  FormControlLabel,
  Checkbox,
  Grid,
} from '@mui/material';
import { Visibility, VisibilityOff, Add, Remove } from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';

type Patient = {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  password: string;
  confirmPassword: string;
  dateOfBirth: Date | null;
  gender: 'male' | 'female' | 'other' | 'prefer_not_to_say';
  street: string;
  city: string;
  state: string;
  zip: string;
  maritalStatus: 'single' | 'married' | 'divorced' | 'widowed' | 'separated';
  occupation: string;
  preferredLanguage: string;
  medicalHistory: string[];
  allergies: string[];
  currentMedications: string[];
  emergencyContact?: {
    name: string;
    phone: string;
    relationship: string;
  };
  insurance?: {
    provider: string;
    policyNumber: string;
    groupNumber: string;
  };
  preferences?: {
    communicationMethod: 'email' | 'phone' | 'sms';
    appointmentReminders: boolean;
    healthTips: boolean;
  };
};

type PatientRegistrationFormProps = {
  open: boolean;
  onClose: () => void;
  onRegister: (patient: Omit<Patient, 'password' | 'confirmPassword'>) => void;
  existingPatients: any[];
};

const commonMedicalConditions = [
  'Hypertension', 'Diabetes Type 1', 'Diabetes Type 2', 'Asthma', 'Arthritis',
  'Heart Disease', 'High Cholesterol', 'Thyroid Disorder', 'Depression',
  'Anxiety', 'Migraine', 'Osteoporosis', 'Cancer', 'COPD', 'Kidney Disease'
];

const commonAllergies = [
  'Penicillin', 'Peanuts', 'Shellfish', 'Eggs', 'Milk', 'Soy', 'Wheat',
  'Tree Nuts', 'Fish', 'Latex', 'Dust Mites', 'Pollen', 'Pet Dander'
];

const commonMedications = [
  'Aspirin', 'Ibuprofen', 'Acetaminophen', 'Lisinopril', 'Metformin',
  'Simvastatin', 'Omeprazole', 'Levothyroxine', 'Amlodipine', 'Metoprolol'
];

const languages = [
  'English', 'Spanish', 'French', 'German', 'Italian', 'Portuguese', 'Chinese (Mandarin)',
  'Chinese (Cantonese)', 'Japanese', 'Korean', 'Arabic', 'Hindi', 'Russian'
];

const PatientRegistrationForm: React.FC<PatientRegistrationFormProps> = ({
  open,
  onClose,
  onRegister,
  existingPatients
}) => {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState(0);
  const [showEmergencyContact, setShowEmergencyContact] = useState(false);
  const [showInsurance, setShowInsurance] = useState(false);
  const [showPreferences, setShowPreferences] = useState(false);
  const [selectedConditions, setSelectedConditions] = useState<string[]>([]);
  const [selectedAllergies, setSelectedAllergies] = useState<string[]>([]);
  const [selectedMedications, setSelectedMedications] = useState<string[]>([]);
  const [customCondition, setCustomCondition] = useState('');
  const [customAllergy, setCustomAllergy] = useState('');
  const [customMedication, setCustomMedication] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isDirty, isValid },
    reset,
    setError,
    watch,
    control,
  } = useForm<Patient>({
    mode: 'onChange',
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: '',
      dateOfBirth: null,
      gender: 'prefer_not_to_say',
      street: '',
      city: '',
      state: '',
      zip: '',
      maritalStatus: 'single',
      occupation: '',
      preferredLanguage: 'English',
      medicalHistory: [],
      allergies: [],
      currentMedications: [],
      emergencyContact: {
        name: '',
        phone: '',
        relationship: '',
      },
      insurance: {
        provider: '',
        policyNumber: '',
        groupNumber: '',
      },
      preferences: {
        communicationMethod: 'email',
        appointmentReminders: true,
        healthTips: false,
      },
    },
  });

  const watchPassword = watch('password');

  // Password strength calculation
  React.useEffect(() => {
    if (watchPassword) {
      let strength = 0;
      if (watchPassword.length >= 8) strength += 25;
      if (/[A-Z]/.test(watchPassword)) strength += 25;
      if (/[a-z]/.test(watchPassword)) strength += 25;
      if (/[0-9]/.test(watchPassword)) strength += 25;
      if (/[^A-Za-z0-9]/.test(watchPassword)) strength += 25;
      setPasswordStrength(Math.min(strength, 100));
    } else {
      setPasswordStrength(0);
    }
  }, [watchPassword]);

  const formatPhoneNumber = (value: string) => {
    const digits = value.replace(/\D/g, '');
    
    if (digits.length >= 10) {
      const countryCode = digits.startsWith('1') ? digits.slice(0, 1) : '1';
      const areaCode = digits.slice(-10, -7);
      const middle = digits.slice(-7, -4);
      const last = digits.slice(-4);
      return `+${countryCode} (${areaCode}) ${middle}-${last}`;
    }
    
    return value;
  };

  const formatZipCode = (value: string) => {
    const digits = value.replace(/\D/g, '');
    if (digits.length > 5) {
      return `${digits.slice(0, 5)}-${digits.slice(5, 9)}`;
    }
    return digits;
  };

  const getPasswordStrengthColor = () => {
    if (passwordStrength < 25) return 'error';
    if (passwordStrength < 50) return 'warning';
    if (passwordStrength < 75) return 'info';
    return 'success';
  };

  const getPasswordStrengthLabel = () => {
    if (passwordStrength < 25) return 'Weak';
    if (passwordStrength < 50) return 'Fair';
    if (passwordStrength < 75) return 'Good';
    return 'Strong';
  };

  const validateAge = (date: Date | null) => {
    if (!date) return 'Date of birth is required';
    const today = new Date();
    const birthDate = new Date(date);
    const age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (birthDate > today) {
      return 'Date of birth cannot be in the future';
    }
    
    if (age < 13 || (age === 13 && monthDiff < 0)) {
      return 'Must be at least 13 years old';
    }
    
    return true;
  };

  const addCondition = (condition: string) => {
    if (condition && !selectedConditions.includes(condition)) {
      setSelectedConditions([...selectedConditions, condition]);
    }
  };

  const removeCondition = (condition: string) => {
    setSelectedConditions(selectedConditions.filter(c => c !== condition));
  };

  const addAllergy = (allergy: string) => {
    if (allergy && !selectedAllergies.includes(allergy)) {
      setSelectedAllergies([...selectedAllergies, allergy]);
    }
  };

  const removeAllergy = (allergy: string) => {
    setSelectedAllergies(selectedAllergies.filter(a => a !== allergy));
  };

  const addMedication = (medication: string) => {
    if (medication && !selectedMedications.includes(medication)) {
      setSelectedMedications([...selectedMedications, medication]);
    }
  };

  const removeMedication = (medication: string) => {
    setSelectedMedications(selectedMedications.filter(m => m !== medication));
  };

  const addCustomCondition = () => {
    if (customCondition.trim()) {
      addCondition(customCondition.trim());
      setCustomCondition('');
    }
  };

  const addCustomAllergy = () => {
    if (customAllergy.trim()) {
      addAllergy(customAllergy.trim());
      setCustomAllergy('');
    }
  };

  const addCustomMedication = () => {
    if (customMedication.trim()) {
      addMedication(customMedication.trim());
      setCustomMedication('');
    }
  };

  const handleRegister = (data: Patient) => {
    // Check for existing email
    if (existingPatients.some((p) => p.email === data.email)) {
      setError('email', { type: 'manual', message: 'Email already exists' });
      return;
    }

    // Check for existing phone
    if (existingPatients.some((p) => p.phone === data.phone)) {
      setError('phone', { type: 'manual', message: 'Phone number already exists' });
      return;
    }

    // Prepare patient data
    const { password, confirmPassword, ...patientData } = data;
    const finalData = {
      ...patientData,
      medicalHistory: selectedConditions,
      allergies: selectedAllergies,
      currentMedications: selectedMedications,
      emergencyContact: showEmergencyContact && data.emergencyContact?.name 
        ? data.emergencyContact 
        : undefined,
      insurance: showInsurance && data.insurance?.provider 
        ? data.insurance 
        : undefined,
      preferences: showPreferences 
        ? data.preferences 
        : undefined,
    };

    onRegister(finalData);
    handleClose();
  };

  const handleClose = () => {
    onClose();
    reset();
    setSelectedConditions([]);
    setSelectedAllergies([]);
    setSelectedMedications([]);
    setCustomCondition('');
    setCustomAllergy('');
    setCustomMedication('');
    setShowEmergencyContact(false);
    setShowInsurance(false);
    setShowPreferences(false);
    setPasswordStrength(0);
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Dialog
        open={open}
        onClose={handleClose}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: 4, p: 2, maxHeight: '90vh', maxWidth: '100vh' } }}
      >
        <DialogTitle>
          <Typography variant="h5" component="h2" sx={{ fontWeight: 600, color: 'primary.main' }}>
            Patient Registration
          </Typography>
        </DialogTitle>
        
        <DialogContent sx={{ pb: 2 }}>
          <Box component="form" onSubmit={handleSubmit(handleRegister)}>
            <Grid container spacing={2}>
              {/* Personal Information */}
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 2, color: 'primary.main' }}>
                  Personal Information
                </Typography>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="First Name"
                  {...register('firstName', {
                    required: 'First name is required',
                    minLength: { value: 2, message: 'First name must be at least 2 characters' },
                    maxLength: { value: 50, message: 'First name must not exceed 50 characters' },
                    pattern: {
                      value: /^[A-Za-z\s'-]+$/,
                      message: 'First name can only contain letters, spaces, hyphens, and apostrophes'
                    }
                  })}
                  error={!!errors.firstName}
                  helperText={errors.firstName?.message}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Last Name"
                  {...register('lastName', {
                    required: 'Last name is required',
                    minLength: { value: 2, message: 'Last name must be at least 2 characters' },
                    maxLength: { value: 50, message: 'Last name must not exceed 50 characters' },
                    pattern: {
                      value: /^[A-Za-z\s'-]+$/,
                      message: 'Last name can only contain letters, spaces, hyphens, and apostrophes'
                    }
                  })}
                  error={!!errors.lastName}
                  helperText={errors.lastName?.message}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  type="email"
                  label="Email"
                  {...register('email', {
                    required: 'Email is required',
                    pattern: {
                      value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                      message: 'Please enter a valid email address'
                    }
                  })}
                  error={!!errors.email}
                  helperText={errors.email?.message}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Phone Number"
                  {...register('phone', {
                    required: 'Phone number is required',
                    pattern: {
                      value: /^\+?[\d\s\(\)\-]+$/,
                      message: 'Please enter a valid phone number'
                    },
                    minLength: { value: 10, message: 'Phone number is too short' }
                  })}
                  error={!!errors.phone}
                  helperText={errors.phone?.message}
                  onChange={(e) => {
                    e.target.value = formatPhoneNumber(e.target.value);
                  }}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <Controller
                  name="dateOfBirth"
                  control={control}
                  rules={{ validate: validateAge }}
                  render={({ field }) => (
                    <DatePicker
                      label="Date of Birth"
                      value={field.value}
                      onChange={field.onChange}
                      maxDate={new Date()}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          error: !!errors.dateOfBirth,
                          helperText: errors.dateOfBirth?.message,
                        },
                      }}
                    />
                  )}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Gender"
                  {...register('gender')}
                >
                  <MenuItem value="male">Male</MenuItem>
                  <MenuItem value="female">Female</MenuItem>
                  <MenuItem value="other">Other</MenuItem>
                  <MenuItem value="prefer_not_to_say">Prefer not to say</MenuItem>
                </TextField>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Marital Status"
                  {...register('maritalStatus')}
                >
                  <MenuItem value="single">Single</MenuItem>
                  <MenuItem value="married">Married</MenuItem>
                  <MenuItem value="divorced">Divorced</MenuItem>
                  <MenuItem value="widowed">Widowed</MenuItem>
                  <MenuItem value="separated">Separated</MenuItem>
                </TextField>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Occupation"
                  {...register('occupation', {
                    maxLength: { value: 100, message: 'Occupation must not exceed 100 characters' }
                  })}
                  error={!!errors.occupation}
                  helperText={errors.occupation?.message}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  select
                  label="Preferred Language"
                  {...register('preferredLanguage')}
                >
                  {languages.map((lang) => (
                    <MenuItem key={lang} value={lang}>{lang}</MenuItem>
                  ))}
                </TextField>
              </Grid>

              {/* Account Security */}
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 2, color: 'primary.main' }}>
                  Account Security
                </Typography>
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControl fullWidth variant="outlined">
                  <InputLabel htmlFor="password">Password</InputLabel>
                  <OutlinedInput
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    {...register('password', {
                      required: 'Password is required',
                      minLength: { value: 8, message: 'Password must be at least 8 characters' },
                      pattern: {
                        value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/,
                        message: 'Password must contain uppercase, lowercase, number, and special character'
                      }
                    })}
                    error={!!errors.password}
                    endAdornment={
                      <InputAdornment position="end">
                        <IconButton
                          onClick={() => setShowPassword(!showPassword)}
                          edge="end"
                        >
                          {showPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    }
                    label="Password"
                  />
                  {errors.password && (
                    <FormHelperText error>{errors.password.message}</FormHelperText>
                  )}
                  {watchPassword && (
                    <Box sx={{ mt: 1 }}>
                      <LinearProgress
                        variant="determinate"
                        value={passwordStrength}
                        color={getPasswordStrengthColor()}
                        sx={{ height: 6, borderRadius: 3 }}
                      />
                      <Typography variant="caption" color={`${getPasswordStrengthColor()}.main`}>
                        Password Strength: {getPasswordStrengthLabel()}
                      </Typography>
                    </Box>
                  )}
                </FormControl>
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControl fullWidth variant="outlined">
                  <InputLabel htmlFor="confirmPassword">Confirm Password</InputLabel>
                  <OutlinedInput
                    id="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    {...register('confirmPassword', {
                      required: 'Please confirm your password',
                      validate: value => value === watchPassword || 'Passwords do not match'
                    })}
                    error={!!errors.confirmPassword}
                    endAdornment={
                      <InputAdornment position="end">
                        <IconButton
                          onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                          edge="end"
                        >
                          {showConfirmPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    }
                    label="Confirm Password"
                  />
                  {errors.confirmPassword && (
                    <FormHelperText error>{errors.confirmPassword.message}</FormHelperText>
                  )}
                </FormControl>
              </Grid>

              {/* Address */}
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 2, color: 'primary.main' }}>
                  Address
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Street Address"
                  {...register('street', {
                    required: 'Street address is required',
                    maxLength: { value: 200, message: 'Street address must not exceed 200 characters' }
                  })}
                  error={!!errors.street}
                  helperText={errors.street?.message}
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="City"
                  {...register('city', {
                    required: 'City is required',
                    maxLength: { value: 100, message: 'City must not exceed 100 characters' }
                  })}
                  error={!!errors.city}
                  helperText={errors.city?.message}
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="State"
                  {...register('state', {
                    required: 'State is required',
                    maxLength: { value: 50, message: 'State must not exceed 50 characters' }
                  })}
                  error={!!errors.state}
                  helperText={errors.state?.message}
                />
              </Grid>

              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="ZIP Code"
                  {...register('zip', {
                    required: 'ZIP code is required',
                    pattern: {
                      value: /^\d{5}(-\d{4})?$/,
                      message: 'Please enter a valid ZIP code (12345 or 12345-6789)'
                    }
                  })}
                  error={!!errors.zip}
                  helperText={errors.zip?.message}
                  onChange={(e) => {
                    e.target.value = formatZipCode(e.target.value);
                  }}
                />
              </Grid>

              {/* Medical History */}
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 2, color: 'primary.main' }}>
                  Medical History
                </Typography>
                
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Select any medical conditions that apply:
                </Typography>

                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
                  {commonMedicalConditions.map((condition) => (
                    <Chip
                      key={condition}
                      label={condition}
                      clickable
                      color={selectedConditions.includes(condition) ? 'primary' : 'default'}
                      onClick={() => {
                        if (selectedConditions.includes(condition)) {
                          removeCondition(condition);
                        } else {
                          addCondition(condition);
                        }
                      }}
                    />
                  ))}
                </Box>

                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                  <TextField
                    size="small"
                    label="Add custom condition"
                    value={customCondition}
                    onChange={(e) => setCustomCondition(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && addCustomCondition()}
                  />
                  <IconButton onClick={addCustomCondition} color="primary">
                    <Add />
                  </IconButton>
                </Box>

                {selectedConditions.length > 0 && (
                  <Box>
                    <Typography variant="subtitle2" sx={{ mb: 1 }}>
                      Selected conditions:
                    </Typography>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                      {selectedConditions.map((condition) => (
                        <Chip
                          key={condition}
                          label={condition}
                          color="primary"
                          onDelete={() => removeCondition(condition)}
                          deleteIcon={<Remove />}
                        />
                      ))}
                    </Box>
                  </Box>
                )}
              </Grid>

              {/* Allergies */}
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 2, color: 'primary.main' }}>
                  Allergies
                </Typography>
                
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Select any allergies you have:
                </Typography>

                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
                  {commonAllergies.map((allergy) => (
                    <Chip
                      key={allergy}
                      label={allergy}
                      clickable
                      color={selectedAllergies.includes(allergy) ? 'secondary' : 'default'}
                      onClick={() => {
                        if (selectedAllergies.includes(allergy)) {
                          removeAllergy(allergy);
                        } else {
                          addAllergy(allergy);
                        }
                      }}
                    />
                  ))}
                </Box>

                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                  <TextField
                    size="small"
                    label="Add custom allergy"
                    value={customAllergy}
                    onChange={(e) => setCustomAllergy(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && addCustomAllergy()}
                  />
                  <IconButton onClick={addCustomAllergy} color="primary">
                    <Add />
                  </IconButton>
                </Box>

                {selectedAllergies.length > 0 && (
                  <Box>
                    <Typography variant="subtitle2" sx={{ mb: 1 }}>
                      Selected allergies:
                    </Typography>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                      {selectedAllergies.map((allergy) => (
                        <Chip
                          key={allergy}
                          label={allergy}
                          color="secondary"
                          onDelete={() => removeAllergy(allergy)}
                          deleteIcon={<Remove />}
                        />
                      ))}
                    </Box>
                  </Box>
                )}
              </Grid>

              {/* Current Medications */}
              <Grid item xs={12}>
                <Typography variant="h6" sx={{ mt: 2, mb: 2, color: 'primary.main' }}>
                  Current Medications
                </Typography>
                
                <Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
                  Select any medications you are currently taking:
                </Typography>

                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
                  {commonMedications.map((medication) => (
                    <Chip
                      key={medication}
                      label={medication}
                      clickable
                      color={selectedMedications.includes(medication) ? 'success' : 'default'}
                      onClick={() => {
                        if (selectedMedications.includes(medication)) {
                          removeMedication(medication);
                        } else {
                          addMedication(medication);
                        }
                      }}
                    />
                  ))}
                </Box>

                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                  <TextField
                    size="small"
                    label="Add custom medication"
                    value={customMedication}
                    onChange={(e) => setCustomMedication(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && addCustomMedication()}
                  />
                  <IconButton onClick={addCustomMedication} color="primary">
                    <Add />
                  </IconButton>
                </Box>

                {selectedMedications.length > 0 && (
                  <Box>
                    <Typography variant="subtitle2" sx={{ mb: 1 }}>
                      Selected medications:
                    </Typography>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                      {selectedMedications.map((medication) => (
                        <Chip
                          key={medication}
                          label={medication}
                          color="success"
                          onDelete={() => removeMedication(medication)}
                          deleteIcon={<Remove />}
                        />
                      ))}
                    </Box>
                  </Box>
                )}
              </Grid>

              {/* Emergency Contact */}
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={showEmergencyContact}
                      onChange={(e) => setShowEmergencyContact(e.target.checked)}
                    />
                  }
                  label="Add Emergency Contact (Optional)"
                />
              </Grid>

              <Collapse in={showEmergencyContact}>
                <Grid container spacing={2} sx={{ mt: 1 }}>
                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="Emergency Contact Name"
                      {...register('emergencyContact.name', {
                        maxLength: { value: 100, message: 'Name must not exceed 100 characters' }
                      })}
                      error={!!errors.emergencyContact?.name}
                      helperText={errors.emergencyContact?.name?.message}
                    />
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="Emergency Contact Phone"
                      {...register('emergencyContact.phone', {
                        pattern: {
                          value: /^\+?[\d\s\(\)\-]+$/,
                          message: 'Please enter a valid phone number'
                        }
                      })}
                      error={!!errors.emergencyContact?.phone}
                      helperText={errors.emergencyContact?.phone?.message}
                      onChange={(e) => {
                        e.target.value = formatPhoneNumber(e.target.value);
                      }}
                    />
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="Relationship"
                      {...register('emergencyContact.relationship', {
                        maxLength: { value: 50, message: 'Relationship must not exceed 50 characters' }
                      })}
                      error={!!errors.emergencyContact?.relationship}
                      helperText={errors.emergencyContact?.relationship?.message}
                    />
                  </Grid>
                </Grid>
              </Collapse>

              {/* Insurance Information */}
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={showInsurance}
                      onChange={(e) => setShowInsurance(e.target.checked)}
                    />
                  }
                  label="Add Insurance Information (Optional)"
                />
              </Grid>

              <Collapse in={showInsurance}>
                <Grid container spacing={2} sx={{ mt: 1 }}>
                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="Insurance Provider"
                      {...register('insurance.provider')}
                    />
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="Policy Number"
                      {...register('insurance.policyNumber')}
                    />
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      label="Group Number"
                      {...register('insurance.groupNumber')}
                    />
                  </Grid>
                </Grid>
              </Collapse>

              {/* Communication Preferences */}
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={showPreferences}
                      onChange={(e) => setShowPreferences(e.target.checked)}
                    />
                  }
                  label="Set Communication Preferences (Optional)"
                />
              </Grid>

              <Collapse in={showPreferences}>
                <Grid container spacing={2} sx={{ mt: 1 }}>
                  <Grid item xs={12} sm={4}>
                    <TextField
                      fullWidth
                      select
                      label="Preferred Communication Method"
                      {...register('preferences.communicationMethod')}
                    >
                      <MenuItem value="email">Email</MenuItem>
                      <MenuItem value="phone">Phone</MenuItem>
                      <MenuItem value="sms">SMS/Text</MenuItem>
                    </TextField>
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <FormControlLabel
                      control={
                        <Controller
                          name="preferences.appointmentReminders"
                          control={control}
                          render={({ field }) => (
                            <Checkbox
                              checked={field.value}
                              onChange={field.onChange}
                            />
                          )}
                        />
                      }
                      label="Appointment Reminders"
                    />
                  </Grid>

                  <Grid item xs={12} sm={4}>
                    <FormControlLabel
                      control={
                        <Controller
                          name="preferences.healthTips"
                          control={control}
                          render={({ field }) => (
                            <Checkbox
                              checked={field.value}
                              onChange={field.onChange}
                            />
                          )}
                        />
                      }
                      label="Health Tips & Newsletter"
                    />
                  </Grid>
                </Grid>
              </Collapse>

              {/* Action Buttons */}
              <Grid item xs={12}>
                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end', mt: 3 }}>
                  <Button
                    onClick={handleClose}
                    color="secondary"
                    variant="outlined"
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    disabled={!isValid || !isDirty}
                    sx={{
                      minWidth: 150,
                      background: 'linear-gradient(45deg, #1976d2 30%, #42a5f5 90%)',
                      '&:hover': {
                        background: 'linear-gradient(45deg, #1565c0 30%, #1976d2 90%)',
                      },
                    }}
                  >
                    Register Patient
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </Box>
        </DialogContent>
      </Dialog>
    </LocalizationProvider>
  );
};

export default PatientRegistrationForm;