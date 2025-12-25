import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../../context/AuthContext';
import { authApi } from '../../api/auth';
import { Button, Input, Card } from '../../components/shared';
import { Header } from '../../components/layout/Header';
import { changePasswordSchema, type ChangePasswordFormData } from '../../utils/validators';
import type { AxiosError } from 'axios';
import type { ErrorResponse } from '../../types/api';

export default function ChangePasswordPage() {
  const navigate = useNavigate();
  const { logout, user } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ChangePasswordFormData>({
    resolver: zodResolver(changePasswordSchema),
  });

  const onSubmit = async (data: ChangePasswordFormData) => {
    setError(null);
    try {
      await authApi.changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      });
      setSuccess(true);
      setTimeout(() => {
        logout();
        navigate('/login', { replace: true });
      }, 2000);
    } catch (err) {
      const axiosError = err as AxiosError<ErrorResponse>;
      setError(axiosError.response?.data?.message || 'Failed to change password');
    }
  };

  const backPath = user?.role === 'ACCOUNTANT' || user?.role === 'ADMIN'
    ? '/accountant'
    : '/client/profile';

  return (
    <div className="min-h-screen bg-gray-50">
      <Header title="Change Password" showBackButton backPath={backPath} />

      <div className="max-w-md mx-auto px-4 py-8">
        <Card>
          {success ? (
            <div className="text-center py-6">
              <div className="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-4">
                <svg
                  className="w-8 h-8 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <h2 className="text-lg font-semibold text-brand-green mb-2">
                Password Changed Successfully
              </h2>
              <p className="text-gray-500">You will be redirected to login...</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {error && (
                <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                  <p className="text-sm text-red-600">{error}</p>
                </div>
              )}

              <Input
                label="Current Password"
                type="password"
                placeholder="Enter current password"
                error={errors.currentPassword?.message}
                {...register('currentPassword')}
              />

              <Input
                label="New Password"
                type="password"
                placeholder="At least 8 characters"
                error={errors.newPassword?.message}
                {...register('newPassword')}
              />

              <Input
                label="Confirm New Password"
                type="password"
                placeholder="Confirm new password"
                error={errors.confirmNewPassword?.message}
                {...register('confirmNewPassword')}
              />

              <Button type="submit" isLoading={isSubmitting} className="w-full">
                Change Password
              </Button>
            </form>
          )}
        </Card>
      </div>
    </div>
  );
}
