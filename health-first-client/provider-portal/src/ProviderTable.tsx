import React, { useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Menu,
  MenuItem,
  Chip,
  Box,
  Typography,
  useMediaQuery
} from '@mui/material';
import { MoreVert, Edit, Delete, Visibility } from '@mui/icons-material';

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
  status?: 'active' | 'inactive';
};

type ProviderTableProps = {
  providers: Provider[];
  onEdit?: (provider: Provider, index: number) => void;
  onDelete?: (index: number) => void;
  onView?: (provider: Provider) => void;
};

const ProviderTable: React.FC<ProviderTableProps> = ({ providers, onEdit, onDelete, onView }) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedProviderIndex, setSelectedProviderIndex] = useState<number | null>(null);
  const isMobile = useMediaQuery('(max-width:768px)');

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, index: number) => {
    setAnchorEl(event.currentTarget);
    setSelectedProviderIndex(index);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedProviderIndex(null);
  };

  const handleEdit = () => {
    if (selectedProviderIndex !== null && onEdit) {
      onEdit(providers[selectedProviderIndex], selectedProviderIndex);
    }
    handleMenuClose();
  };

  const handleDelete = () => {
    if (selectedProviderIndex !== null && onDelete) {
      onDelete(selectedProviderIndex);
    }
    handleMenuClose();
  };

  const handleView = () => {
    if (selectedProviderIndex !== null && onView) {
      onView(providers[selectedProviderIndex]);
    }
    handleMenuClose();
  };

  const getStatusColor = (status: string = 'active') => {
    return status === 'active' ? 'success' : 'error';
  };

  const formatAddress = (provider: Provider) => {
    return `${provider.street}, ${provider.city}, ${provider.state} ${provider.zip}`;
  };

  if (isMobile) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: '100%' }}>
        {providers.map((provider, index) => (
          <Paper key={index} sx={{ 
            p: 2, 
            display: 'flex', 
            flexDirection: 'column', 
            gap: 1,
            boxShadow: 2,
            borderRadius: 2,
            border: '1px solid #e0e0e0',
            '&:hover': {
              boxShadow: 4,
              borderColor: '#1976d2'
            },
            transition: 'all 0.2s ease'
          }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <Box>
                <Typography variant="h6" color="primary.main" sx={{ fontWeight: 600 }}>
                  {provider.firstName} {provider.lastName}
                </Typography>
                <Chip 
                  label={provider.specialization} 
                  size="small" 
                  sx={{ 
                    backgroundColor: '#e3f2fd', 
                    color: '#1976d2',
                    fontWeight: 500,
                    mt: 0.5
                  }} 
                />
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Chip 
                  label={provider.status || 'active'} 
                  color={getStatusColor(provider.status) as any}
                  size="small"
                  sx={{ fontWeight: 500 }}
                />
                <IconButton
                  size="small"
                  onClick={(e) => handleMenuOpen(e, index)}
                  sx={{ 
                    color: '#666',
                    '&:hover': { 
                      backgroundColor: '#e3f2fd',
                      color: '#1976d2'
                    }
                  }}
                >
                  <MoreVert />
                </IconButton>
              </Box>
            </Box>
            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1, mt: 1 }}>
              <Typography variant="body2" sx={{ color: '#666' }}>
                <strong>Email:</strong> {provider.email}
              </Typography>
              <Typography variant="body2" sx={{ color: '#666' }}>
                <strong>Phone:</strong> {provider.phone}
              </Typography>
              <Typography variant="body2" sx={{ color: '#666' }}>
                <strong>License:</strong> {provider.license}
              </Typography>
              <Typography variant="body2" sx={{ color: '#666' }}>
                <strong>Experience:</strong> {provider.experience} years
              </Typography>
            </Box>
            <Typography variant="body2" sx={{ color: '#666', mt: 1 }}>
              <strong>Address:</strong> {formatAddress(provider)}
            </Typography>
          </Paper>
        ))}
      </Box>
    );
  }

  return (
    <TableContainer component={Paper} sx={{ 
      maxHeight: '100%', 
      overflow: 'auto',
      width: '100%',
      boxShadow: 3,
      borderRadius: 2,
      '& .MuiTable-root': {
        minWidth: 1200
      }
    }}>
      <Table stickyHeader>
        <TableHead>
          <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
            <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5', color: '#333' }}>Name</TableCell>
            <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5', color: '#333' }}>Email</TableCell>
            <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5', color: '#333' }}>Phone</TableCell>
            <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5', color: '#333' }}>Specialization</TableCell>
            <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5', color: '#333' }}>License</TableCell>
            <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5', color: '#333' }}>Experience</TableCell>
            <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5', color: '#333' }}>Address</TableCell>
            <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5', color: '#333' }}>Status</TableCell>
            <TableCell sx={{ fontWeight: 'bold', backgroundColor: '#f5f5f5', color: '#333' }}>Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {providers.map((provider, index) => (
            <TableRow key={index} hover sx={{ 
              '&:nth-of-type(odd)': { backgroundColor: '#fafafa' },
              '&:hover': { backgroundColor: '#f0f8ff' }
            }}>
              <TableCell>
                <Typography variant="body2" sx={{ fontWeight: 500, color: '#1976d2' }}>
                  {provider.firstName} {provider.lastName}
                </Typography>
              </TableCell>
              <TableCell>{provider.email}</TableCell>
              <TableCell>{provider.phone}</TableCell>
              <TableCell>
                <Chip 
                  label={provider.specialization} 
                  size="small" 
                  sx={{ 
                    backgroundColor: '#e3f2fd', 
                    color: '#1976d2',
                    fontWeight: 500
                  }} 
                />
              </TableCell>
              <TableCell sx={{ fontFamily: 'monospace', fontWeight: 500 }}>{provider.license}</TableCell>
              <TableCell>{provider.experience} years</TableCell>
              <TableCell sx={{ maxWidth: 200 }}>
                <Typography variant="body2" noWrap title={formatAddress(provider)}>
                  {formatAddress(provider)}
                </Typography>
              </TableCell>
              <TableCell>
                <Chip 
                  label={provider.status || 'active'} 
                  color={getStatusColor(provider.status) as any}
                  size="small"
                  sx={{ fontWeight: 500 }}
                />
              </TableCell>
              <TableCell>
                <IconButton
                  size="small"
                  onClick={(e) => handleMenuOpen(e, index)}
                  sx={{ 
                    color: '#666',
                    '&:hover': { 
                      backgroundColor: '#e3f2fd',
                      color: '#1976d2'
                    }
                  }}
                >
                  <MoreVert />
                </IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
        sx={{
          '& .MuiMenuItem-root': {
            py: 1.5,
            px: 2
          }
        }}
      >
        <MenuItem onClick={handleView}>
          <Visibility sx={{ mr: 1, color: '#1976d2' }} />
          View Details
        </MenuItem>
        <MenuItem onClick={handleEdit}>
          <Edit sx={{ mr: 1, color: '#ff9800' }} />
          Edit
        </MenuItem>
        <MenuItem onClick={handleDelete} sx={{ color: 'error.main' }}>
          <Delete sx={{ mr: 1 }} />
          Delete
        </MenuItem>
      </Menu>
    </TableContainer>
  );
};

export default ProviderTable; 