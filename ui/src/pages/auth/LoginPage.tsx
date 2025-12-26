import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../../hooks/useAuth';
import { Button, Input, Card } from '../../components/shared';
import { loginSchema, type LoginFormData } from '../../utils/validators';
import type { AxiosError } from 'axios';
import type { ErrorResponse } from '../../types/api';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, user } = useAuth();
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    setError(null);
    try {
      await login(data);
      const redirectPath = user?.role === 'ACCOUNTANT' || user?.role === 'ADMIN'
        ? '/accountant'
        : '/client';
      navigate(redirectPath, { replace: true });
    } catch (err) {
      const axiosError = err as AxiosError<ErrorResponse>;
      const message = axiosError.response?.data?.message;

      if (message?.includes('pending approval')) {
        setError('Your account is pending admin approval. You will be notified via email once approved.');
      } else if (message?.includes('rejected')) {
        setError('Your account registration has been rejected. Please contact support.');
      } else {
        setError(message || 'Invalid email or password');
      }
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-gray-50">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-brand-green">Tax Portal</h1>
          <p className="text-gray-600 mt-2">Sign in to your account</p>
        </div>

        <Card>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            {error && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            <Input
              label="Email"
              type="email"
              placeholder="Enter your email"
              error={errors.email?.message}
              {...register('email')}
            />

            <Input
              label="Password"
              type="password"
              placeholder="Enter your password"
              error={errors.password?.message}
              {...register('password')}
            />

            <div className="flex items-center justify-end">
              <Link
                to="/forgot-password"
                className="text-sm text-brand-green hover:underline"
              >
                Forgot password?
              </Link>
            </div>

            <Button type="submit" isLoading={isSubmitting} className="w-full">
              Sign In
            </Button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-gray-600">
              Don't have an account?{' '}
              <Link to="/register" className="text-brand-green font-medium hover:underline">
                Sign up
              </Link>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
}
