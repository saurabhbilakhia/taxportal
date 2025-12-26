import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../../hooks/useAuth';
import { Button, Input, Card } from '../../components/shared';
import { clientRegisterSchema, type ClientRegisterFormData } from '../../utils/validators';
import type { AxiosError } from 'axios';
import type { ErrorResponse, ValidationErrorResponse } from '../../types/api';

export default function ClientRegisterPage() {
  const navigate = useNavigate();
  const { registerClient } = useAuth();
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ClientRegisterFormData>({
    resolver: zodResolver(clientRegisterSchema),
  });

  const onSubmit = async (data: ClientRegisterFormData) => {
    setError(null);
    try {
      await registerClient({
        email: data.email,
        password: data.password,
        firstName: data.firstName,
        lastName: data.lastName,
        phone: data.phone,
      });
      navigate('/client', { replace: true });
    } catch (err) {
      const axiosError = err as AxiosError<ErrorResponse | ValidationErrorResponse>;
      if (axiosError.response?.data) {
        const responseData = axiosError.response.data;
        if ('errors' in responseData) {
          const firstError = Object.values(responseData.errors)[0];
          setError(firstError || 'Registration failed');
        } else {
          setError(responseData.message || 'Registration failed');
        }
      } else {
        setError('Registration failed. Please try again.');
      }
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-8 bg-gray-50">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-brand-green">Client Registration</h1>
          <p className="text-gray-600 mt-2">Create your client account</p>
        </div>

        <Card>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            {error && (
              <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="First Name"
                placeholder="John"
                error={errors.firstName?.message}
                {...register('firstName')}
              />
              <Input
                label="Last Name"
                placeholder="Doe"
                error={errors.lastName?.message}
                {...register('lastName')}
              />
            </div>

            <Input
              label="Email"
              type="email"
              placeholder="john@example.com"
              error={errors.email?.message}
              {...register('email')}
            />

            <Input
              label="Phone (optional)"
              type="tel"
              placeholder="(555) 123-4567"
              error={errors.phone?.message}
              {...register('phone')}
            />

            <Input
              label="Password"
              type="password"
              placeholder="At least 8 characters"
              error={errors.password?.message}
              {...register('password')}
            />

            <Input
              label="Confirm Password"
              type="password"
              placeholder="Confirm your password"
              error={errors.confirmPassword?.message}
              {...register('confirmPassword')}
            />

            <Button type="submit" isLoading={isSubmitting} className="w-full">
              Create Client Account
            </Button>
          </form>

          <div className="mt-6 text-center space-y-2">
            <p className="text-gray-600">
              Are you an accountant?{' '}
              <Link to="/register/accountant" className="text-brand-green font-medium hover:underline">
                Register as accountant
              </Link>
            </p>
            <p className="text-gray-600">
              Already have an account?{' '}
              <Link to="/login" className="text-brand-green font-medium hover:underline">
                Sign in
              </Link>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
}
