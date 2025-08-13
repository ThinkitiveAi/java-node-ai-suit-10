


import React, { useState } from 'react';
import { useForm, Controller, useFieldArray } from 'react-hook-form';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Button,
  Typography,
  Box,
  Switch,
  FormControlLabel,
  IconButton,
  Card,
  CardContent,
  Chip
} from '@mui/material';
import { Calendar, Clock, Plus, X, Save, User, Delete } from 'lucide-react';

// Static data for providers with UUIDs
const PROVIDERS = [
  { id: '550e8400-e29b-41d4-a716-446655440000', name: 'Dr. John Doe' },
  { id: '550e8400-e29b-41d4-a716-446655440001', name: 'Dr. Jane Smith' },
  { id: '550e8400-e29b-41d4-a716-446655440002', name: 'Dr. Michael Johnson' },
  { id: '550e8400-e29b-41d4-a716-446655440003', name: 'Dr. Sarah Williams' },
  { id: '550e8400-e29b-41d4-a716-446655440004', name: 'Dr. Emily Davis' }
];

const DAYS_OF_WEEK = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

const TIME_ZONES = [
  'America/New_York',
  'America/Chicago', 
  'America/Denver',
  'America/Los_Angeles',
  'Europe/London',
  'Europe/Paris',
  'Asia/Tokyo',
  'Asia/Kolkata'
];

const defaultFormValues = {
  providerId: '',
  timeZone: 'America/New_York',
  availability: DAYS_OF_WEEK.map(day => ({
    day,
    fromTime: '09:00',
    toTime: '18:00',
    enabled: day !== 'Sunday'
  })),
  blockDays: []
};

type ProviderAvailabilityProps = {
  open: boolean;
  onClose: () => void;
};

const ProviderAvailability: React.FC<ProviderAvailabilityProps> = ({ open, onClose }) => {
  const { control, handleSubmit, watch, reset, formState: { errors } } = useForm({
    defaultValues: defaultFormValues
  });

  const { fields: blockDayFields, append: addBlockDay, remove: removeBlockDay } = useFieldArray({
    control,
    name: 'blockDays'
  });

  const watchedProviderId = watch('providerId');
  const selectedProviderData = PROVIDERS.find(p => p.id === watchedProviderId);

  const handleClose = () => {
    reset();
    onClose();
  };

  const onSubmit = (data) => {
    const formData = {
      providerId: data.providerId,
      providerName: PROVIDERS.find(p => p.id === data.providerId)?.name,
      timeZone: data.timeZone,
      availability: data.availability.filter(day => day.enabled),
      blockDays: data.blockDays
    };
    console.log('Saving provider availability:', formData);
    alert('Provider availability saved successfully!');
    handleClose();
  };

  const handleAddBlockDay = () => {
    addBlockDay({
      date: '',
      fromTime: '09:00',
      toTime: '17:00'
    });
  };

  return (
    <Dialog 
      open={open} 
      onClose={handleClose} 
      maxWidth="lg" 
      fullWidth
      PaperProps={{ 
        sx: { 
          borderRadius: 4, 
          p: 2,
          maxHeight: '90vh'
        } 
      }}
    >
      <DialogTitle sx={{ 
        textAlign: 'center', 
        fontWeight: 700, 
        fontSize: '1.5rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 1
      }}>
        <User size={24} />
        Provider Availability Management
      </DialogTitle>
      
      <DialogContent sx={{ p: 3 }}>
        <form onSubmit={handleSubmit(onSubmit)}>
          <Grid container spacing={3}>
            {/* Provider Selection */}
            <Grid item xs={12}>
              <Controller
                name="providerId"
                control={control}
                rules={{ required: 'Provider selection is required' }}
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.providerId}>
                    <InputLabel>Select Provider</InputLabel>
                    <Select
                      {...field}
                      label="Select Provider"
                    >
                      {PROVIDERS.map(provider => (
                        <MenuItem key={provider.id} value={provider.id}>
                          {provider.name}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}
              />
              {selectedProviderData && (
                <Box mt={2}>
                  <Chip 
                    label={`Selected: ${selectedProviderData.name}`}
                    color="primary"
                    variant="outlined"
                  />
                  <Typography variant="caption" display="block" sx={{ mt: 1, color: 'text.secondary' }}>
                    ID: {selectedProviderData.id}
                  </Typography>
                </Box>
              )}
            </Grid>

            <Grid item xs={12} md={8}>
              {/* Day-wise Availability */}
              <Card sx={{ mb: 2 }}>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Calendar size={20} />
                    Day Wise Availability
                  </Typography>

                  {DAYS_OF_WEEK.map((day, index) => (
                    <Card key={day} sx={{ mb: 1, bgcolor: 'grey.50' }}>
                      <CardContent sx={{ p: 2 }}>
                        <Grid container spacing={2} alignItems="center">
                          <Grid item xs={12} sm={3}>
                            <Typography variant="subtitle2" fontWeight={600}>
                              {day}
                            </Typography>
                          </Grid>
                          
                          <Grid item xs={6} sm={3}>
                            <Controller
                              name={`availability.${index}.fromTime`}
                              control={control}
                              render={({ field }) => (
                                <TextField
                                  {...field}
                                  type="time"
                                  label="From"
                                  size="small"
                                  fullWidth
                                  InputLabelProps={{ shrink: true }}
                                  disabled={!watch(`availability.${index}.enabled`)}
                                />
                              )}
                            />
                          </Grid>
                          
                          <Grid item xs={6} sm={3}>
                            <Controller
                              name={`availability.${index}.toTime`}
                              control={control}
                              render={({ field }) => (
                                <TextField
                                  {...field}
                                  type="time"
                                  label="Till"
                                  size="small"
                                  fullWidth
                                  InputLabelProps={{ shrink: true }}
                                  disabled={!watch(`availability.${index}.enabled`)}
                                />
                              )}
                            />
                          </Grid>
                          
                          <Grid item xs={12} sm={3}>
                            <Controller
                              name={`availability.${index}.enabled`}
                              control={control}
                              render={({ field }) => (
                                <FormControlLabel
                                  control={
                                    <Switch 
                                      checked={field.value}
                                      onChange={(e) => field.onChange(e.target.checked)}
                                      color="primary"
                                    />
                                  }
                                  label={field.value ? 'Enabled' : 'Disabled'}
                                />
                              )}
                            />
                          </Grid>
                        </Grid>
                      </CardContent>
                    </Card>
                  ))}
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} md={4}>
              {/* Time Zone Selection */}
              <Card sx={{ mb: 2 }}>
                <CardContent>
                  <Typography variant="h6" sx={{ mb: 2 }}>
                    Slot Creation Setting
                  </Typography>
                  
                  <Controller
                    name="timeZone"
                    control={control}
                    render={({ field }) => (
                      <FormControl fullWidth>
                        <InputLabel>Time Zone</InputLabel>
                        <Select
                          {...field}
                          label="Time Zone"
                        >
                          {TIME_ZONES.map(tz => (
                            <MenuItem key={tz} value={tz}>
                              {tz}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                    )}
                  />
                </CardContent>
              </Card>

              {/* Block Days */}
              <Card>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                    <Typography variant="h6">
                      Block Days
                    </Typography>
                    <Button
                      onClick={handleAddBlockDay}
                      startIcon={<Plus size={16} />}
                      variant="outlined"
                      size="small"
                    >
                      Add Block Day
                    </Button>
                  </Box>

                  {blockDayFields.length === 0 ? (
                    <Box textAlign="center" py={4} color="text.secondary">
                      <Calendar size={48} style={{ opacity: 0.3, marginBottom: 12 }} />
                      <Typography variant="body2">
                        No blocked days configured
                      </Typography>
                    </Box>
                  ) : (
                    blockDayFields.map((field, index) => (
                      <Card key={field.id} sx={{ mb: 2, bgcolor: 'grey.50' }}>
                        <CardContent sx={{ p: 2 }}>
                          <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                            <Typography variant="subtitle2">
                              Block Day {index + 1}
                            </Typography>
                            <IconButton
                              onClick={() => removeBlockDay(index)}
                              size="small"
                              color="error"
                            >
                              <Delete size={16} />
                            </IconButton>
                          </Box>
                          
                          <Grid container spacing={1}>
                            <Grid item xs={12}>
                              <Controller
                                name={`blockDays.${index}.date`}
                                control={control}
                                render={({ field }) => (
                                  <TextField
                                    {...field}
                                    type="date"
                                    label="Date"
                                    size="small"
                                    fullWidth
                                    InputLabelProps={{ shrink: true }}
                                  />
                                )}
                              />
                            </Grid>
                            
                            <Grid item xs={6}>
                              <Controller
                                name={`blockDays.${index}.fromTime`}
                                control={control}
                                render={({ field }) => (
                                  <TextField
                                    {...field}
                                    type="time"
                                    label="From"
                                    size="small"
                                    fullWidth
                                    InputLabelProps={{ shrink: true }}
                                  />
                                )}
                              />
                            </Grid>
                            
                            <Grid item xs={6}>
                              <Controller
                                name={`blockDays.${index}.toTime`}
                                control={control}
                                render={({ field }) => (
                                  <TextField
                                    {...field}
                                    type="time"
                                    label="Till"
                                    size="small"
                                    fullWidth
                                    InputLabelProps={{ shrink: true }}
                                  />
                                )}
                              />
                            </Grid>
                          </Grid>
                        </CardContent>
                      </Card>
                    ))
                  )}
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </form>
      </DialogContent>

      <DialogActions sx={{ p: 3, gap: 1 }}>
        <Button
          onClick={handleClose}
          variant="outlined"
          color="secondary"
        >
          Close
        </Button>
        <Button
          onClick={handleSubmit(onSubmit)}
          variant="contained"
          startIcon={<Save size={16} />}
          disabled={!watchedProviderId}
        >
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ProviderAvailability;