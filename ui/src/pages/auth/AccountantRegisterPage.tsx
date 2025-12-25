import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../../context/AuthContext';
import { Button, Input, Card } from '../../components/shared';
import { accountantRegisterSchema, type AccountantRegisterFormData } from '../../utils/validators';
import type { AxiosError } from 'axios';
import type { ErrorResponse, ValidationErrorResponse } from '../../types/api';

export default function AccountantRegisterPage() {
  const { registerAccountant } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [registrationComplete, setRegistrationComplete] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<AccountantRegisterFormData>({
    resolver: zodResolver(accountantRegisterSchema),
  });

  const onSubmit = async (data: AccountantRegisterFormData) => {
    setError(null);
    try {
      await registerAccountant({
        email: data.email,
        password: data.password,
        firstName: data.firstName,
        lastName: data.lastName,
        phone: data.phone,
        licenseNumber: data.licenseNumber,
        firmName: data.firmName,
      });
      setRegistrationComplete(true);
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

  if (registrationComplete) {
    return (
      <div className="min-h-screen flex items-center justify-center px-4 py-8 bg-gray-50">
        <div className="w-full max-w-md">
          <Card>
            <div className="text-center py-6">
              <div className="w-16 h-16 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg
                  className="w-8 h-8 text-amber-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
              <h2 className="text-2xl font-bold text-brand-green mb-2">
                Registration Submitted
              </h2>
              <p className="text-gray-600 mb-4">
                Your accountant registration is pending admin approval. We will notify you
                via email once your account has been approved.
              </p>
              <p className="text-sm text-gray-500 mb-6">
                This typically takes 1-2 business days.
              </p>
              <Link to="/login">
                <Button variant="secondary" className="w-full">
                  Return to Login
                </Button>
              </Link>
            </div>
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-8 bg-gray-50">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-brand-green">Accountant Registration</h1>
          <p className="text-gray-600 mt-2">Create your professional account</p>
        </div>

        <div className="mb-6 p-4 bg-amber-50 border border-amber-200 rounded-lg">
          <p className="text-sm text-amber-800">
            <strong>Note:</strong> Accountant accounts require admin approval before
            activation. You will be notified via email once approved.
          </p>
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

            <div className="border-t pt-5 mt-5">
              <h3 className="text-sm font-medium text-brand-green mb-4">
                Professional Information
              </h3>

              <div className="space-y-5">
                <Input
                  label="License Number"
                  placeholder="CPA-123456"
                  error={errors.licenseNumber?.message}
                  {...register('licenseNumber')}
                />

                <Input
                  label="Firm Name"
                  placeholder="ABC Accounting LLC"
                  error={errors.firmName?.message}
                  {...register('firmName')}
                />
              </div>
            </div>

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
              Submit Registration
            </Button>
          </form>

          <div className="mt-6 text-center space-y-2">
            <p className="text-gray-600">
              Not an accountant?{' '}
              <Link to="/register/client" className="text-brand-green font-medium hover:underline">
                Register as client
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
